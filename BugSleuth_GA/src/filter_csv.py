import sys
import pandas as pd

def filter_csv(csv_path, txt_path):
    # Read CSV and TXT files
    df = pd.read_csv(csv_path, delimiter=',')
    with open(txt_path, 'r') as txt_file:
        txt_values = [line.strip() for line in txt_file]

    # Filter rows based on 'concat' column
    df = df[df.iloc[:, 3].isin(txt_values)]

    # Save the filtered dataframe back to the CSV file
    df.to_csv('GT_prepare_LLMAO_comparison_input.csv', sep=',', index=False)

def main():
    if len(sys.argv) != 3:
        print("Usage: python script.py <csv_file> <txt_file>")
        sys.exit(1)

    csv_file = sys.argv[1]
    txt_file = sys.argv[2]

    filter_csv(csv_file, txt_file)
    print("Filtering complete.")

if __name__ == "__main__":
    main()
