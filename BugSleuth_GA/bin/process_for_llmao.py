import os
import sys

def process_file(file_path, class_name):
    lines_to_keep = []
    with open(file_path, 'r') as file:
        for line in file:
            # Extract text until the first '#'
            line_content = line.split('#')[0].strip()
            if class_name in line_content:
                lines_to_keep.append(line.strip())
    return lines_to_keep

def main():
    if len(sys.argv) != 3:
        print("Usage: python script.py <directory_path>")
        sys.exit(1)

    directory_path = sys.argv[1]

    ground_truth_file = sys.argv[2]
    with open(ground_truth_file, 'r') as gt_file:
        next(gt_file)  # Skip header
        for line in gt_file:
            project, bug, class_path, _ = line.strip().split(',')
            class_name_start = class_path.lower().find('org') if project.lower() != 'closure' else class_path.lower().find('com')
            class_name = class_path[class_name_start:].replace('/', '.').replace('.java', '')
            
            project_path = os.path.join(directory_path, project.lower())
            bug_path = os.path.join(project_path, str(bug))

            susps_file_path = os.path.join(bug_path, 'stmt-susps.txt')

            if os.path.exists(susps_file_path):
                print("project", project)
                print("bug", bug)
                print("class", class_name)
                print("exists")
                lines_to_keep = process_file(susps_file_path, class_name)

                # Check if the number of lines to keep is less than 5
                if len(lines_to_keep) >= 5:
                    # Write the filtered lines back to the file
                    with open(susps_file_path, 'w') as susps_file:
                        susps_file.write('\n'.join(lines_to_keep))
                else:
                    print("Not making changes, as lines to keep are less than 5.")

if __name__ == "__main__":
    main()
