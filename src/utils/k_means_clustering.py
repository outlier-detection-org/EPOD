from sklearn.cluster import KMeans
import pandas as pd
import numpy as np
import random
data = pd.read_table('../../../Datasets/tao_1.txt',sep=',',header=None)
df=pd.DataFrame(data)
X = np.array(df)
n_clusters = 4
size = 500
mix_rate = 20

kmeans = KMeans(n_clusters=n_clusters, random_state=0).fit(X)
file = open("../../../Datasets/tao_kmeans_clustering"+"_"+str(mix_rate)+".txt","w+")
clusters = []
for i in range(n_clusters):
    clusters.append([])
for i in range(kmeans.labels_.size):
    clusters[kmeans.labels_[i]].append(X[i])
for i in range(n_clusters):
    print(len(clusters[i]))
    
#mix 10% 0->1 1->2 2->3 3->0
count =int(size * mix_rate/100)
before = clusters[0][0:count]
for i in range(n_clusters):
    j = (i+1)%n_clusters
    after =  clusters[j][0:count]
    clusters[j][0:count] = before
    before = after
for i in range(n_clusters):
    random.shuffle(clusters[i])
    
for x in range(20+5-1):
    for i in range(n_clusters):
        for j in range(int(size/n_clusters)):
            index = int(x*size/n_clusters+j)
            file.write(str(i))
            file.write(",")
            for j in range(clusters[i][index].size-1):
                file.write(str(clusters[i][index][j]))
                file.write(",")
            file.write(str(clusters[i][index][len(clusters[i][index])-1]))
            file.write("\n")
            