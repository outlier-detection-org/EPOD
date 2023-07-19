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
mix_rate_node = 0.5
mix_rate_client = 0.5
data = pd.read_table('..\\..\\Datasets\\stock.txt', sep=',', header=None)
prefix = "..\\..\\Datasets\\DeviceId_data\\Node_" \
         + str(n_nodes) + "_Device_" + str(n_clients) + "_STK_K_" + str(mix_rate_node) + "\\"

# read datasets
df = pd.DataFrame(data)
X = np.array(df)

# step 1: cluster based on node number, and mixture;
kmeans = KMeans(n_clusters=n_nodes).fit(X)
clusters = []
for i in range(n_nodes):
    clusters.append([])
for i in range(kmeans.labels_.size):
    clusters[kmeans.labels_[i]].append(X[i])

for i in range(n_nodes):
    random.shuffle(clusters[i])

# mixture
group = []
for i in range(n_nodes):
    count = int(len(clusters[i]) * mix_rate_node)
    for j in range(count):
        group.append(clusters[i][j])
random.shuffle(group)

# reassign
clusters1 = []
for i in range(n_nodes):
    clusters1.append([])

left = 0
right = int(len(group) / n_nodes)
for i in range(n_nodes):
    # mixed part
    for j in range(left, right):
        clusters1[i].append(group[j])
    # original part
    count = int(len(clusters[i]) * mix_rate_node)
    for j in range(count + 1, len(clusters[i])):
        clusters1[i].append(clusters[i][j])
    left = right
    if i == n_nodes - 2:
        right = len(group)
    else:
        right = int(left + len(group) / n_nodes)

for i in range(n_nodes):
    random.shuffle(clusters1[i])

clusters2 = []
# step 2: for each node, cluster based on client number, and mixture
for i in range(n_nodes):
    kmeans = KMeans(n_clusters=n_clients).fit(clusters1[i])
    clusters = []
    for j in range(n_clients):
        clusters.append([])
    for j in range(kmeans.labels_.size):
        clusters[kmeans.labels_[j]].append(clusters1[i][j])
    for j in range(n_clients):
        random.shuffle(clusters[j])
    # mixture
    group = []
    for j in range(n_clients):
        count = int(len(clusters[j]) * mix_rate_client)
        for k in range(count):
            group.append(clusters[j][k])
    random.shuffle(group)

    # reassign
    for j in range(n_clients):
        clusters2.append([])

    left = 0
    right = int(len(group) / n_clients)
    for j in range(n_clients):
        # mixed part
        for k in range(left, right):
            clusters2[i * n_clients + j].append(group[k])
        # original part
        count = int(len(clusters[j]) * mix_rate_client)
        for k in range(count + 1, len(clusters[j])):
            clusters2[i * n_clients + j].append(clusters[j][k])
        left = right
        if i == n_clients - 2:
            right = len(group)
        else:
            right = int(left + len(group) / n_clients)

    for j in range(n_clients):
        random.shuffle(clusters2[i * n_clients + j])

# print to file
try:
    os.mkdir(prefix, 0o0755)
except OSError:
    print()

files = []
for i in range(n_nodes * n_clients):
    file = open(prefix + str(i) + ".txt", "w")
    files.append(file)

for i in range(n_nodes * n_clients):
    for j in range(len(clusters2[i])):
        files[i].write(str(i) + ",")
        for k in range(len(clusters2[i][j])):
            if k == len(clusters2[i][j]) - 1:
                files[i].write(str(clusters2[i][j][k]))
            else:
                files[i].write(str(clusters2[i][j][k]) + ",")
        files[i].write("\n")

for i in range(n_nodes):
    files[i].close()
