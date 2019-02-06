import sys
import json
import os 
import wfdb 
from wfdb import processing


inbound = json.loads(sys.argv[1])
ecg = inbound['ecg_values']
for i in range(len(ecg)):
    ecg[i] = float(ecg[i])
# ecg_str = inbound['ecg']
# ecg = list()
# for val in ecg_str:
#     ecg.append(float(val))
  
qrs_peaks = wfdb.processing.find_local_peaks(ecg, 159)
p_qrs_t = wfdb.processing.find_local_peaks(ecg, 43)

sum , num = 0, 0
for i in range(0, len(p_qrs_t) - 2):
    try:
        sum+= list(p_qrs_t).index(qrs_peaks[i + 1]) - (list(p_qrs_t).index(qrs_peaks[i])) -1
        num+=1
    except:
        pass
avg = sum/num

if avg < 1.8:
    print("Atrial Fibrillation")
    sys.stdout.flush()
else:
    print("Normal rhythm")
    sys.stdout.flush()




