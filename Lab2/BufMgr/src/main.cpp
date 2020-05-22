#include <iostream>
#include <stdlib.h>
//#include <stdio.h>
#include <cstring>
#include <memory>
#include "page.h"
#include "buffer.h"
#include "file_iterator.h"
#include "page_iterator.h"
#include "exceptions/file_not_found_exception.h"
#include "exceptions/invalid_page_exception.h"
#include "exceptions/page_not_pinned_exception.h"
#include "exceptions/page_pinned_exception.h"
#include "exceptions/buffer_exceeded_exception.h"

#define PRINT_ERROR(str) \
{ \
	std::cerr << "On Line No:" << __LINE__ << "\n"; \
	std::cerr << str << "\n"; \
	exit(1); \
}

using namespace badgerdb;

const PageId num = 100;
PageId pid[num], pageno1, pageno2, pageno3, i;
RecordId rid[num], rid2, rid3;
Page *page, *page2, *page3;
char tmpbuf[100];
BufMgr* bufMgr;
File *file1ptr, *file2ptr, *file3ptr, *file4ptr, *file5ptr;

void test1();
void test2();
void test3();
void test4();
void test5();
void test6();
void testBufMgr();

int main() 
{
	//Following code shows how to you File and Page classes

  const std::string& filename = "test.db";
  // Clean up from any previous runs that crashed.
  // 清除上一次测试的残余文件
  try
	{
    File::remove(filename);
  }
	catch(FileNotFoundException)
	{
  }

  {
    // Create a new database file.
	// 创建一个新文件用于测试
    File new_file = File::create(filename);
    
    // Allocate some pages and put data on them.
	// 申请一些页面，并在页面中加入数据
    PageId third_page_number;
    for (int i = 0; i < 5; ++i) {
      Page new_page = new_file.allocatePage();
      if (i == 3) {
        // Keep track of the identifier for the third page so we can read it
        // later. 跟踪第三个页面
        third_page_number = new_page.page_number();
      }
      new_page.insertRecord("hello!");
      // Write the page back to the file (with the new data).
	  // 将新页面写回文件中
      new_file.writePage(new_page);
    }

	// 循环输出一下现在的页面上都有什么内容
    // Iterate through all pages in the file.
    for (FileIterator iter = new_file.begin();
         iter != new_file.end();
         ++iter) {
      // Iterate through all records on the page.
	  Page p = *iter;
      for (PageIterator page_iter = p.begin();
           page_iter != p.end();
           ++page_iter) {
        std::cout << "Found record: " << *page_iter
            << " on page " << (*iter).page_number() << "\n";
      }
    }

    // Retrieve the third page and add another record to it.
	// 在页面三上再加入一条数据world!
    Page third_page = new_file.readPage(third_page_number);
    const RecordId& rid = third_page.insertRecord("world!");
    new_file.writePage(third_page);

    // Retrieve the record we just added to the third page.
    std::cout << "Third page has a new record: "
        << third_page.getRecord(rid) << "\n\n";
  }
  // new_file goes out of scope here, so file is automatically closed.

  // Delete the file since we're done with it.
  File::remove(filename);

	//This function tests buffer manager, comment this line if you don't wish to test buffer manager
	// 开始测试过程
	testBufMgr();
}

void testBufMgr()
{
	// create buffer manager
  // 创建一个新的buffer manager 能够缓冲100个page
	bufMgr = new BufMgr(num);

	// create dummy files
	// 创建一些虚拟文件
  const std::string& filename1 = "test.1";
  const std::string& filename2 = "test.2";
  const std::string& filename3 = "test.3";
  const std::string& filename4 = "test.4";
  const std::string& filename5 = "test.5";
	// 删除上一次测试时存在的文件
  try
	{
    File::remove(filename1);
    File::remove(filename2);
    File::remove(filename3);
    File::remove(filename4);
    File::remove(filename5);
  }
	catch(FileNotFoundException e)
	{
  }

	File file1 = File::create(filename1);
	File file2 = File::create(filename2);
	File file3 = File::create(filename3);
	File file4 = File::create(filename4);
	File file5 = File::create(filename5);

	file1ptr = &file1;
	file2ptr = &file2;
	file3ptr = &file3;
	file4ptr = &file4;
	file5ptr = &file5;

	//Test buffer manager
	//Comment tests which you do not wish to run now. Tests are dependent on their preceding tests. So, they have to be run in the following order. 
	//Commenting  a particular test requires commenting all tests that follow it else those tests would fail.
	test1();
	test2();
	test3();
	test4();
	test5();
	test6();

	//Close files before deleting them
	file1.~File();
	file2.~File();
	file3.~File();
	file4.~File();
	file5.~File();

	//Delete files
	File::remove(filename1);
	File::remove(filename2);
	File::remove(filename3);
	File::remove(filename4);
	File::remove(filename5);

	delete bufMgr;

	std::cout << "\n" << "Passed all tests." << "\n";
}


// 所有的allocPage都调用allocBuf 即时针算法
// readPage 当要读取的页面不存在于pool中时 有可能调用allocBuf
// 检查点 -- 收到数据页请求时 返回正确的结果
// 检查点 -- 时钟算法
void test1()
{
	//Allocating pages in a file...
  // 在文件中申请页面
	for (i = 0; i < num; i++)
	{ // 申请一个页面
		bufMgr->allocPage(file1ptr, pid[i], page);
    // 将这个动作生成一个字符串，然后写入到这个页面中
		sprintf((char*)tmpbuf, "test.1 Page %d %7.1f", pid[i], (float)pid[i]);
		rid[i] = page->insertRecord(tmpbuf);
    // 解锁这个页面
		bufMgr->unPinPage(file1ptr, pid[i], true);
	}

  // 检查点 -- 请求的页在pool中 返回指针
  // 检查点 -- 正确访问pool中的页面
	// Reading pages back...
  // 然后从读取这些页面 看是否正确
	for (i = 0; i < num; i++)
	{ // 读取页面函数 看是否能返回正确的结果
		bufMgr->readPage(file1ptr, pid[i], page);
    // 构造页面上的字符串内容进行比较
		sprintf((char*)&tmpbuf, "test.1 Page %d %7.1f", pid[i], (float)pid[i]);
		if(strncmp(page->getRecord(rid[i]).c_str(), tmpbuf, strlen(tmpbuf)) != 0)
		{ // 如果比较失败则会报错 说明没有返回正确的页面
			PRINT_ERROR("ERROR :: CONTENTS DID NOT MATCH");
		}
    // 不需要这个页面 解锁这个页面
		bufMgr->unPinPage(file1ptr, pid[i], false);
	}
	std::cout<< "Test 1 passed" << "\n";
}

// 检查点 -- 收到数据页请求时 返回正确的结果
// 请求的页面不在pool中 正确更新了页面并返回
// 检查点 -- 时钟算法
void test2()
{
	//Writing and reading back multiple files
	//The page number and the value should match

	for (i = 0; i < num/3; i++) 
	{ // 依旧是申请页面 然后构造页面数据 这里是文件2
    // 需要注意是 现在的pool中是文件1的页面 所以会进行替换 
		bufMgr->allocPage(file2ptr, pageno2, page2);
		sprintf((char*)tmpbuf, "test.2 Page %d %7.1f", pageno2, (float)pageno2);
		rid2 = page2->insertRecord(tmpbuf);
    // 随机提取文件1中的页面进行比较 看能否正确读出文件页面
    // 这个时候 有些被文件2的页面覆盖掉了 有些没有 所以这里是一个readPage的检查点
		int index = rand() % num;
    	pageno1 = pid[index];
		bufMgr->readPage(file1ptr, pageno1, page);
		sprintf((char*)tmpbuf, "test.1 Page %d %7.1f", pageno1, (float)pageno1);
		if(strncmp(page->getRecord(rid[index]).c_str(), tmpbuf, strlen(tmpbuf)) != 0)
		{
			PRINT_ERROR("ERROR :: CONTENTS DID NOT MATCH");
		}
    // 申请页面 然后构造页面数据 这里是文件3
		bufMgr->allocPage(file3ptr, pageno3, page3);
		sprintf((char*)tmpbuf, "test.3 Page %d %7.1f", pageno3, (float)pageno3);
		rid3 = page3->insertRecord(tmpbuf);
    // 提取文件2中的页面进行比较 看能否正确读出文件页面
		bufMgr->readPage(file2ptr, pageno2, page2);
		sprintf((char*)&tmpbuf, "test.2 Page %d %7.1f", pageno2, (float)pageno2);
		if(strncmp(page2->getRecord(rid2).c_str(), tmpbuf, strlen(tmpbuf)) != 0)
		{
			PRINT_ERROR("ERROR :: CONTENTS DID NOT MATCH");
		}
    // 提取文件3中的页面进行比较 看能否正确读出文件页面
		bufMgr->readPage(file3ptr, pageno3, page3);
		sprintf((char*)&tmpbuf, "test.3 Page %d %7.1f", pageno3, (float)pageno3);
		if(strncmp(page3->getRecord(rid3).c_str(), tmpbuf, strlen(tmpbuf)) != 0)
		{
			PRINT_ERROR("ERROR :: CONTENTS DID NOT MATCH");
		}
    // 不需要这个页面 解锁这个页面 这里是文件1的页面
    // 因为文件1的页面没有被修改
		bufMgr->unPinPage(file1ptr, pageno1, false);
	}
  // 不需要这个页面 解锁这个页面 但是这里dirty位设置为true了 需要写回磁盘
  // 文件2 文件3的修改需要被记录下来
	for (i = 0; i < num/3; i++) {
		bufMgr->unPinPage(file2ptr, i+1, true);
		bufMgr->unPinPage(file2ptr, i+1, true);
		bufMgr->unPinPage(file3ptr, i+1, true);
		bufMgr->unPinPage(file3ptr, i+1, true);
	}

	std::cout << "Test 2 passed" << "\n";
}

// 检查点 -- 访问页面不正确时抛出异常
void test3()
{
  // 尝试读一个不存在的页面 应该抛出Exception
	try
	{
		bufMgr->readPage(file4ptr, 1, page);
		PRINT_ERROR("ERROR :: File4 should not exist. Exception should have been thrown before execution reaches this point.");
	}
	catch(InvalidPageException e)
	{
	}

	std::cout << "Test 3 passed" << "\n";
}

// 检查点 -- 页面锁定
void test4()
{
  // unPinPage一个已经unPinPage的页面
  // 测试unPinPage是否能正确抛出异常
	bufMgr->allocPage(file4ptr, i, page);
	bufMgr->unPinPage(file4ptr, i, true);
	try
	{
		bufMgr->unPinPage(file4ptr, i, false);
		PRINT_ERROR("ERROR :: Page is already unpinned. Exception should have been thrown before execution reaches this point.");
	}
	catch(PageNotPinnedException e)
	{
	}
  // 不需要这个页面 解锁这个页面 但是这里dirty位设置为true了 需要写回磁盘
	std::cout << "Test 4 passed" << "\n";
}

// 对应检查点 -- 时钟算法的Exception
void test5()
{
  // 申请页面 然后构造页面数据 这里是文件5 这里弄满了缓冲区 刚好100个
	for (i = 0; i < num; i++) {
		bufMgr->allocPage(file5ptr, pid[i], page);
		sprintf((char*)tmpbuf, "test.5 Page %d %7.1f", pid[i], (float)pid[i]);
		rid[i] = page->insertRecord(tmpbuf);
	}
  // 尝试再申请一个页面 将会抛出Exception 因为缓冲区已经没有可以新申请的页面了
  // 这里是按照了时钟算法来检查 因为新页面的pinCnt=1(调用了set函数) 所以所有的页面都是pinned的
  // 没有unpinned的页面 时钟算法将会抛出BufferExceededException
	PageId tmp;
	try
	{
		bufMgr->allocPage(file5ptr, tmp, page);
		PRINT_ERROR("ERROR :: No more frames left for allocation. Exception should have been thrown before execution reaches this point.");
	}
	catch(BufferExceededException e)
	{
	}

	std::cout << "Test 5 passed" << "\n";

	for (i = 1; i <= num; i++)
		bufMgr->unPinPage(file5ptr, i, true);
}

// 对应检查点 -- 是否正确将数据写回磁盘
void test6()
{
	//flushing file with pages still pinned. Should generate an error
  // 将文件1的页面读入到缓冲区中 由于之前缓冲区里头全是文件5的页面
  // 这里要从磁盘读入 会使用set函数 使得PinCnt=1
	for (i = 1; i <= num; i++) {
		bufMgr->readPage(file1ptr, i, page);
	}

	try
	{ // 尝试删除缓冲区内的文件1 但是由于存在Pinned的页面
    // 所以会抛出 PagePinnedException
		bufMgr->flushFile(file1ptr);
		PRINT_ERROR("ERROR :: Pages pinned for file being flushed. Exception should have been thrown before execution reaches this point.");
	}
	catch(PagePinnedException e)
	{
	}

	std::cout << "Test 6 passed" << "\n";
  // 然后解锁文件1中的所有页面
	for (i = 1; i <= num; i++) 
		bufMgr->unPinPage(file1ptr, i, true);
  // 再次尝试删除缓冲区内的文件1 现在不存在Pinned的页面了
  // 所以正常删除结束
	bufMgr->flushFile(file1ptr);
}
