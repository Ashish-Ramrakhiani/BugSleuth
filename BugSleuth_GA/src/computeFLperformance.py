import os
import sys
import re 
import os.path
from collections import OrderedDict

AllDefects = {}
ground_truth_path = sys.argv[1]
dataset_path = sys.argv[2]
fl_results_path = sys.argv[3]
proj = sys.argv[4]
k = sys.argv[5]

class Defect(object):
    def __init__(self, project, bug_id):
         self.project = project
         self.bug_id = bug_id
         self.dev_modifications = {} 
         self.fl_results = {} 

    def storeDeveloperModifiedInformation(self, modified_file, modified_lines):
        distinct_modified_lines = []
        if len(modified_lines) > 0:
            for l in modified_lines:
                distinct_modified_lines.append(int(l))
        self.dev_modifications[modified_file] = distinct_modified_lines

# store defects in dataset
dataset = open(dataset_path) 
dataset_defects = []
for line in dataset:
    dataset_defects.append(line.strip().lower())
dataset.close()

# store ground truth information
ground_truth = open(ground_truth_path)
next(ground_truth)
for line in ground_truth:
    record = line.split(",")
    project = record[0]
    bug_id = record[1]
    defect = project + "-" + bug_id
    
    proj_list = proj.strip().split(',')
    if len(proj) > 0 and project.lower() not in proj_list:
        continue
    
    if defect.lower() not in dataset_defects:
        continue 
    
    d = Defect(project, bug_id)
    if defect not in AllDefects:
        AllDefects[defect] = d
    modified_file = record[2]
    modified_line = record[3].strip()
   # print(defect, modified_file, modified_line)
    if ";" in modified_line:
        modified_lines = modified_line.split(";")
    elif len(modified_line)>0:
        modified_lines = [modified_line]
    AllDefects[defect].storeDeveloperModifiedInformation(modified_file, modified_lines)
ground_truth.close()
print("Dataset Size:", len(AllDefects))


def computeFLMetrics(topk):
    sum_exam = 0.0
    localized_defects = []
    for defect in sorted(AllDefects):
        d = AllDefects[defect]
        defect = d.project + "-" + d.bug_id
        localized = False
        total_inspected = 0
        exam = 1.0

	
            
        fl_file_path = fl_results_path + "/" + d.project.lower() + "/" + str(d.bug_id ) + "/stmt-susps.txt"
        #print("=>", fl_file_path) 
        if not os.path.exists(fl_file_path):
            continue
           
        with open(fl_file_path, 'r') as f:
            lines = f.readlines()
            
        total_statements = len(lines)
        index = 0 
            
        for stmt in lines:
            #score = sorted_fl_results[stmt]
            stmt = stmt.strip()
            
            modified_file = stmt.split("#")[0].replace(".", "/") + ".java"
            modified_line = int(stmt.split("#")[1].strip())
        
            index += 1
            for fl in d.dev_modifications:
                if fl.find(modified_file) != -1:  
                    if (modified_line) in d.dev_modifications[fl]:
                        total_inspected += 1
                        localized = True
                        break 
                
            if not localized:
                total_inspected += 1
            if localized or index == topk:
                break

        if total_inspected == 0.0:
            total_inspected = 1.0

        if topk > 1 and topk < total_statements:
            exam = float(total_inspected)/float(topk)
        else:
            if topk == 1 and localized:
                exam = 0.01
            else:
                if localized:
                    exam = float(total_inspected)/float(total_statements)
                else:
                    exam = 1.0
        if localized:
            sum_exam += exam
            if defect not in localized_defects:
                localized_defects.append(defect)
                #print(defect)
    print("top-N: ", len(localized_defects), " out of ", len(AllDefects))
    if len(localized_defects) > 0:
        print("Avg EXAM: ", round(float(sum_exam)/float(len(localized_defects)),3))
    else:    
        print("Avg EXAM: NA")
    #print("Localized defects",localized_defects)


topk = [1, 3, 5 ]
if k == 'all':
    for tk in topk:
        print("Computing results for top-" + str(tk))
        computeFLMetrics(tk)
else:
    topk = k.split(",")
    for tk in topk:
        print("Computing results for top-" + tk)
        computeFLMetrics(int(tk))
