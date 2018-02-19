import matplotlib.pyplot as plt
import numpy as np

timingFile = open('screenCaptureTiming.txt', 'r')
times = timingFile.read().split('\n')
times = filter(lambda x: x is not '', times)
# print(times)

# Convert to floats
timesflt = map(float, times) 
# Normalize the array so the first time is zero
timesflt = map(lambda x: x - timesflt[0], timesflt)

# We want to plot the differences, not the actual time values.
diffs = []
for i in range(0, (len(timesflt) - 1)):
  diffs.append(timesflt[i+1] - timesflt[i])


plt.plot(diffs, 'ro')
plt.title('Timestamps of Screen Capture Over Time')
plt.xlabel('timestamp number')
plt.ylabel('time to next capture')

plt.show()
# print(timesflt)
avgTimeBetweenTS = reduce((lambda x, y: x + y), diffs)
avgTimeBetweenTS = avgTimeBetweenTS/len(diffs)
print('Average time between timestamps: ' +str(avgTimeBetweenTS))

