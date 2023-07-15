import os
from sklearn.cluster import KMeans
import pandas as pd
import numpy as np
import random

# top-down: first cluster based on node number, and mixture;
# Then for each mixed cluster, cluster based on client number, and mixture

# Configuration that need to be changed each time
n_nodes = 4
n_clients = 8
mix_rate = 0.5
data = pd.read_table('C:\\Users\\Lenovo\\Desktop\\outlier_detection\\EPOD\\Datasets\\tao.txt', sep=',', header=None)
prefix = "C:\\Users\\Lenovo\\Desktop\\outlier_detection\\EPOD\\Datasets\\DeviceId_data\\Node_" \
         + str(n_nodes) + "_Device_" + str(n_clients) + "_TAO_K_" + str(mix_rate) + "\\"

# read datasets
df = pd.DataFrame(data)
X = np.array(df)

# step 1: cluster based on node number, and mixture;
kmeans = KMeans(n_clusters=n_nodes, random_state=0).fit(X)
clusters = []
for i in range(n_nodes):
    clusters.append([])
for i in range(kmeans.labels_.size):
    clusters[kmeans.labels_[i]].append(X[i])

# mixture
group = []
for i in range(n_nodes):
    count = int(len(clusters[i]) * mix_rate)
    for j in range(count):
            group.append(clusters[i][j])
random.shuffle(group)

# reassign
clusters1 = []
for i in range(n_nodes):
    clusters1.append([])

left = 0
right = int(len(group)/n_nodes)
for i in range(n_nodes):
    # mixed part
    for j in range(left,right):
        clusters1.append(group[j])
    # original part
    count = int(len(clusters[i]) * mix_rate)
    for j in range (count + 1 , len(clusters[i])):
        clusters1.append(clusters[i][j])
    left = right
    right = int(left + len(group)/n_nodes)
    if(i == n_nodes - 2):
        right = len(group)


for i in range(n_nodes):
    random.shuffle(clusters1[i])


os.mkdir(prefix, 0o0755)
files = []
for i in range(n_nodes):
    file = open(prefix + str(i) + ".txt", "w+")
    files.append(file)

for i in range(n_nodes):
    for j in range(len(clusters1[i])):
        files[i].write(str(i)+",")
        for k in range(len(clusters1[i][j])):
            if k == len(clusters1[i][j]) - 1:
                files[i].write(str(clusters1[i][j][k]))
            else:
                files[i].write(str(clusters1[i][j][k])+",")
        files[i].write("\n")

for i in range(n_nodes):
    files[i].close()

