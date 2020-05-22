/**
 * @author See Contributors.txt for code contributors and overview of BadgerDB.
 *
 * @section LICENSE
 * Copyright (c) 2012 Database Group, Computer Sciences Department, University of Wisconsin-Madison.
 */

#include <memory>
#include <iostream>
#include "buffer.h"
#include "exceptions/buffer_exceeded_exception.h"
#include "exceptions/page_not_pinned_exception.h"
#include "exceptions/page_pinned_exception.h"
#include "exceptions/bad_buffer_exception.h"
#include "exceptions/hash_not_found_exception.h"

namespace badgerdb { 

BufMgr::BufMgr(std::uint32_t bufs)
	: numBufs(bufs) {
	bufDescTable = new BufDesc[bufs];

  for (FrameId i = 0; i < bufs; i++) 
  {
  	bufDescTable[i].frameNo = i;
  	bufDescTable[i].valid = false;
  }

  bufPool = new Page[bufs];

	int htsize = ((((int) (bufs * 1.2))*2)/2)+1;
  hashTable = new BufHashTbl (htsize);  // allocate the buffer hash table

  clockHand = bufs - 1;
}

BufMgr::~BufMgr() {
  // 析构函数 写回全部的valid dirty的页面
  // To check if the buffer is a dirty page
  for(std::uint32_t i = 0; i < numBufs; i++)
  {
    BufDesc* buffer = &bufDescTable[i];
    // if is dirty, write page into the disk
    if(buffer->dirty && buffer->valid)
    {
      buffer->file->writePage(bufPool[buffer->frameNo]);
      buffer->dirty = false;
    }
    
  }
  // delete all the attr
  delete[] bufPool;
  delete[] bufDescTable;   
  delete hashTable;
}

void BufMgr::advanceClock()
{
  clockHand = (clockHand + 1) % numBufs;
}


void BufMgr::allocBuf(FrameId & frame) 
{
  // 开始检查的start位置
  FrameId st = clockHand;
  // 全部pinned页面标记
  bool not_pinned = false;
  
  while(true)
  {
    advanceClock();
    BufDesc* cur = &bufDescTable[clockHand];

    // 时针走过一圈，检查是否有unpinned页面出现
    if(clockHand == st)
    {
      if(!not_pinned)
      {
        throw BufferExceededException();
      }
      else 
      {
        not_pinned = false;
      }
    }

    if(cur->pinCnt < 1)
    {
      not_pinned = true;
    }
    // 首先检查是否valid
    // 是 -- 进一步检查
    // 否 -- 对Frame调用Set()方法 
    if(cur->valid)
    {
      // 检查是否为refbit set
      // 是 -- 清空refbit continue
      // 否 -- 下一步检查
      if(cur->refbit)
      {
        cur->refbit = 0;
        continue;
      }

      // 检查Page是否pinned
      // 是 -- continue
      // 否 -- 下一步检查
      if(cur->pinCnt > 0)
      {
        continue;
      }

      // 检查dirty位是否有标记
      // 是 -- 将Page重写回磁盘
      // 否 -- 下一步
      if(cur->dirty)
      {
        cur->file->writePage(bufPool[clockHand]);
      }
      hashTable->remove(cur->file, cur->pageNo);
    }
    frame = clockHand;
    cur->Clear();
    return;
  }
}

void BufMgr::readPage(File* file, const PageId pageNo, Page*& page)
{
  FrameId frameNo;
  try
  {
    // 使用lookup()寻找页面，可能抛出HashNotFoundException
    hashTable->lookup(file, pageNo, frameNo);
  }
  catch(HashNotFoundException e)
  {
    // 如果一个页面不存在与缓冲池中，
    // 调用allocBuf()来申请一个buffer Frame帧
    allocBuf(frameNo);
    // 然后使用file->readPage()方法将页面从磁盘加载到buffer帧中
    bufPool[frameNo] = file->readPage(pageNo);
    // 将该页面加入到HashTablke中
    hashTable->insert(file, pageNo, frameNo);

    BufDesc* buf = &bufDescTable[frameNo];
    // 最终使用Set()使该帧正确set up
    buf->Set(file, pageNo);
    // 使用page指针返回该页面
    page = &bufPool[frameNo];
    return;
  }
  // 如果该页面存在于缓冲池中,
  // 正确set up refbit
  // pinCnt 增加 1
  // 使用page指针返回该页面
  BufDesc* buf = &bufDescTable[frameNo];
  buf->refbit = true;
  buf->pinCnt += 1;
  page = &bufPool[frameNo];
  return;
}


void BufMgr::unPinPage(File* file, const PageId pageNo, const bool dirty) 
{
  FrameId frameNo;
  try
  {
    // 使用lookup()寻找页面，可能抛出HashNotFoundException
    hashTable->lookup(file, pageNo, frameNo);
  }
  catch(HashNotFoundException e)
  {
    // 如果没有找到页面，do nothing
    return;
  }
  
  BufDesc* buffer = &bufDescTable[frameNo];
  // 如果dirty位是true，设置页面的dirty位为true
  if(dirty)
  {
    buffer->dirty = true;
  }
  // 如果pin计数已经是0，抛出异常PAGENOPINNED
  if(buffer->pinCnt < 1)
  {
    throw PageNotPinnedException(file->filename(), pageNo, frameNo);
  }
  // 减少pin计数
  buffer->pinCnt -= 1;
  return;
}

void BufMgr::flushFile(const File* file) 
{
  // 检查每个页面，找到该页面
  for(std::uint32_t i = 0; i < numBufs; i++)
  {
    BufDesc* buffer = &bufDescTable[i];

    if(buffer->file == file)
    {
      // 如果这个页面不合法，抛出异常
      if(!buffer->valid)
      {
        throw BadBufferException(buffer->frameNo, buffer->dirty, buffer->valid, buffer->refbit);
      }
      
      // 如果这个页面仍pinned，抛出异常
      if(buffer->pinCnt)
      {
        throw PagePinnedException(file->filename(), buffer->pageNo, buffer->frameNo);
      }
      // 如果这个页面ditry，将页面信息写回磁盘
      if(buffer->dirty)
      {
        buffer->file->writePage(bufPool[buffer->frameNo]);
        buffer->dirty = false;
      }
      // 从hashTable中除去该页面，调用Clear()
      hashTable->remove(buffer->file, buffer->pageNo);
      buffer->Clear();
    }
  }
}

void BufMgr::allocPage(File* file, PageId &pageNo, Page*& page) 
{
  // 使用file->allocatePage获得一个页面
  Page nPage = file->allocatePage();
  
  // 使用allocBuf方法获得一个帧
  FrameId frameNo; 
  allocBuf(frameNo);
  // 将页面信息参数返回
  pageNo = nPage.page_number();
  bufPool[frameNo] = nPage;
  
  // 将页面加入到hashTable中
  // 使用Set正确set up
  hashTable->insert(file, pageNo, frameNo);
  bufDescTable[frameNo].Set(file, pageNo);
  page = &bufPool[frameNo];
  return;
}

void BufMgr::disposePage(File* file, const PageId PageNo)
{
  // 寻找该页面
  for(std::uint32_t i = 0; i < numBufs; i++)
  {
    BufDesc* buffer = &bufDescTable[i];
    if(buffer->file == file && buffer->pageNo == PageNo)
    {
      hashTable->remove(buffer->file, buffer->pageNo);
      buffer->Clear();
    }
  }
  // 从文件中也删除该页面
  file->deletePage(PageNo);
  return;
}

void BufMgr::printSelf(void) 
{
  BufDesc* tmpbuf;
	int validFrames = 0;
  
  for (std::uint32_t i = 0; i < numBufs; i++)
	{
  	tmpbuf = &(bufDescTable[i]);
		std::cout << "FrameNo:" << i << " ";
		tmpbuf->Print();

  	if (tmpbuf->valid == true)
    	validFrames++;
  }

	std::cout << "Total Number of Valid Frames:" << validFrames << "\n";
}

}
