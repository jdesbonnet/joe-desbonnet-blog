

import matplotlib.pyplot as plt

# Constants A0, B1, B2, B3, B4, B5
A0 = 3.120790493E+02
B1 = 2.681652834E+00
B2 = -8.061777879E-04
B3 = -1.052906745E-05
B4 = 1.925845957E-08
B5 = -7.465510101E-12

# Function to read data from a file and convert it to a list of integers
def read_data(filename):
    with open(filename, 'r') as file:
        line = file.readline()
        return list(map(int, line.strip().split(',')))

# Read the data from both files
data_a = read_data('a.txt')
data_b = read_data('b.txt')

# Ensure both files have the same length
if len(data_a) != len(data_b):
    raise ValueError("The files do not contain the same number of elements.")

# Compute the difference between the two data sets
difference = [b - a for a, b in zip(data_a, data_b)]

# Generate the X values based on the complex formula: X = A0 + B1*i + B2*i^2 + B3*i^3 + B4*i^4 + B5*i^5
x_values = [
    A0 + B1 * i + B2 * i**2 + B3 * i**3 + B4 * i**4 + B5 * i**5
    for i in range(1, len(difference) + 1)
]

for x in x_values:
    print (f"{x:.1f}")


#output_filename = 'output_data.txt'
#with open(output_filename, 'w') as outfile:
#   outfile.write("X, Difference\n")  # Write header
for x, diff in zip(x_values, difference):
        #outfile.write(f"{x}, {diff}\n")
        print(f"{x:.1f} {diff}")

# Plot the difference
plt.plot(x_values, difference, marker='o', linestyle='-', color='r')
plt.plot(x_values, data_a, marker='o', linestyle='-', color='b')
plt.plot(x_values, data_b, marker='o', linestyle='-', color='g')
# Set titles and labels
plt.title("Lightning spectrum")
plt.xlabel("Wavelength nm")
plt.ylabel("Intensity (ADC units)")

# Show the plot
plt.show()


