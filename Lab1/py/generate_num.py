import random
import os

n = 20
IDs = dict()
# Generate doctor ID

IDs['dID'] = random.choices(range(100000, 200000), k=20)
# Generate patient ID
IDs['pID'] = random.choices(range(500000, 999999), k=20)
# Generate sick bed ID
IDs['bID'] = random.choices(range(1000, 5000), k=20)
# Generate nurse ID
IDs['nID'] = random.choices(range(300000, 400000), k=20)
# Generate Department ID
IDs['deID'] = random.choices(range(1, 30), k=20)
# Generate surgery ID
IDs['sID'] = random.choices(range(10000, 50000), k=20)
# Generate bill ID
IDs['biID'] = random.choices(range(1000000, 9999999), k=20)
# Generate Diagnosis ID
IDs['diaID'] = random.choices(range(1000000, 9999999), k=20)

with open(os.path.join('..',  'data', 'ID.txt'), 'w') as f:
	for k, v in zip(IDs.keys(), IDs.values()):
		f.write(str(k)+'\n')
		f.write(str(v))
		f.write('\n')
