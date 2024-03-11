import subprocess
import csv

def run_command(command):
    try:
        result = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        stdout, stderr = result.communicate()
        if result.returncode == 0:
            return stdout.strip().split("\n")
        else:
            print("Error:", stderr.strip())
            return None
    except Exception as e:
        print("An error occurred:", e)
        return None

def write_to_file(data):
    file_path = "results1.csv"
    try:
        with open(file_path, mode='a', newline='') as file:
            writer = csv.writer(file)
            writer.writerow(data)
    except Exception as e:
        print(f"An error occurred while writing to the CSV file '{file_path}': {e}")

def read_second_line(file_path):
    try:
        with open(file_path, 'r') as file:
            file.readline()
            second_line = file.readline().strip().split(",")
            return second_line[0]
    except FileNotFoundError:
        print(f"Error: File '{file_path}' not found.")
        return None
    except Exception as e:
        print(f"An error occurred while reading the file '{file_path}': {e}")
        return None

def process_rank(ranked_list, result):
    for i in range(len(ranked_list)):
        if ranked_list[i] == result:
            return (i + 1)
    return len(ranked_list) + 1


    
def main():
    initial_command = "java -cp commons-math3-3.6.1/commons-math3-3.6.1.jar:. CrossEntropyMonteCarlo "
    final_command = "_all 2 /nfs/stak/users/bourassn/cs569/LLMAO_input/SBFL_results_2/ /nfs/stak/users/bourassn/cs569/LLMAO_input/blues_results_2/ 4000 0.03 30"
    
    defect_names = ["chart", "closure",  "lang", "math", "mockito", "time"]
    N = 4000
    rho = 0.03
    it = 30
   #command = initial_command + str(N_values[0]) + " " + str(rho_values[0]) + " " + str(iteration_values[0])
   # print(run_command(command))
    for i in range(len(defect_names)):
        command = initial_command + defect_names[i] + final_command
        print(run_command(command))

    
    '''
    this_N = 12000
    this_rho = 0.06
    these_it = [90, 100]
    for i in range(len(these_it)):
        command = initial_command + str(this_N) + " " + str(this_rho) + " " + str(these_it[i])
        print(run_command(command))

    these_rhos = [ 0.07, 0.08, 0.09, 0.1]
    for i in range(len(these_rhos)):
        for j in range(len(iteration_values)):
            command = initial_command + str(this_N) + " " + str(these_rhos[i]) + " " + str(iteration_values[j])
            print(run_command(command))
    

    for i in range(len(N_values)):
        for j in range(len(rho_values)):
            for k in range(len(iteration_values)):
                command = initial_command + str(N_values[i]) + " " + str(rho_values[j]) + " " + str(iteration_values[k])
                print(run_command(command))'''
                
    '''


    num_faults = 112
    results = []
    for i in range(num_faults):
        if i + 1 != 58:
            file_path = initial_directory + perfectFL + str(i + 1) + file_name
            results.append(read_second_line(file_path))
        else:
            results.append("N/A")
        
    write_to_file(["N", "Rho", "Convergence Threshold", "Max Iterations", "Consecutive Small Changes", "Top-1", "Top-3", "Top-5"])
    
    '''

    '''
    for i in range(len(N_values)):
        current_N = " " + str(N_values[i])
        for j in range(len(rho_values)):
            N_to_rho = current_N + " " + str(rho_values[j])
            for k in range(len(convergence_values)):
                N_to_convergence = N_to_rho + " " + str(convergence_values[k])
                for m in range(len(itteration_values)):
                    N_to_itteration = N_to_convergence + " " + str(itteration_values[m])
                    for n in range(len(changes_values)):
                        N_to_changes = N_to_itteration + " " + str(changes_values[n])
                        top_1 = 0
                        top_3 = 0
                        top_5 = 0
                        for o in range(num_faults):
                            if o + 1 != 58:
                                command = initial_command + str(o + 1) + rank_file_name + " " + blues + str(o + 1) + rank_file_name + N_to_changes
                                ranked_list = run_command(command)
                                print("list:")
                                print(ranked_list)
                                print("real:")
                                print(results[o])
                                print("")
                                rank = process_rank(ranked_list, results[o])
                                if rank == 1:
                                    top_1 += 1
                                if rank <= 3:
                                    top_3 += 1
                                if rank <= 5:
                                    top_5 += 1
                        write_values = [str(N_values[i]), str(rho_values[j]), str(convergence_values[k]), str(itteration_values[m]), str(changes_values[n]), str(top_1), str(top_3), str(top_5)]
                        write_to_file(write_values)'''


main()