import os
import sys

def list_defects(path):
    defect_list = []

    # Iterate over subfolders in the given path
    for defect_folder in os.listdir(path):
        defect_path = os.path.join(path, defect_folder)

        # Check if it's a directory
        if os.path.isdir(defect_path):
            # Iterate over subfolders (defect ids) within the defect folder
            for defect_id_folder in os.listdir(defect_path):
                defect_id_path = os.path.join(defect_path, defect_id_folder)

                # Check if it's a directory
                if os.path.isdir(defect_id_path):
                    # Append the defect name and id to the list
                    defect_list.append(f"{defect_folder}-{defect_id_folder}")

    return defect_list

# Example usage:
results_path = sys.argv[1]
defects = list_defects(results_path)
print("Total defects ",len(defects))

# Print the list of defects
for defect in defects:
    print(defect)
