import pandas as pd
import matplotlib.pyplot as plt
from sklearn import preprocessing
from numpy.random import default_rng
import numpy as np
from kmodes.kmodes import KModes
pd.options.mode.chained_assignment = None  # default='warn'

# https://medium.com/geekculture/the-k-modes-as-clustering-algorithm-for-categorical-data-type-bcde8f95efd7
# https://www.analyticsvidhya.com/blog/2021/06/kmodes-clustering-algorithm-for-categorical-data/


def c2(dataset):
    df = pd.DataFrame(dataset)

    # Select the categorical columns
    cols = df.select_dtypes('object').columns
    df_cat = df[cols]
    df_cat.drop(columns=["Employer_Country", "Full_Time_Position", "Case_Status", "Unit_Of_Pay", "Visa_Class"], inplace=True)
    df_cat_copy = df_cat

    # Fill any empty cells with NULL
    df_cat = pd.DataFrame(df_cat).fillna("NULL")
    le = preprocessing.LabelEncoder()
    df_cat = df_cat.apply(le.fit_transform)

    cost = []
    for num_clusters in list(range(1, 40)):
        kmode = KModes(n_clusters=num_clusters, init="Cao", n_init=1, verbose=1)
        kmode.fit_predict(df_cat)
        cost.append(kmode.cost_)

    y = np.array([i for i in range(1, 40, 1)])
    plt.plot(y, cost)
    plt.show()

    km_cao = KModes(n_clusters=20, init="Cao", n_init=1, verbose=1)
    fitClusters_cao = km_cao.fit_predict(df_cat)

    df_cat = df_cat_copy.reset_index()

    clustersDf = pd.DataFrame(fitClusters_cao)
    clustersDf.columns = ['cluster_predicted']

    combinedDf = pd.concat([df_cat, clustersDf], axis=1).reset_index()
    combinedDf = combinedDf.drop(['index', 'level_0'], axis=1)

    combinedClusters = combinedDf.pivot_table(index='cluster_predicted', aggfunc=lambda x: " : ".join(x))

    totalCombinedDF = pd.concat([df, clustersDf], axis=1).reset_index()
    totalCombinedDF = totalCombinedDF.drop(['index'], axis=1)

    # Add clustered data to the dataset for publishing
    for num in list(range(0, 19)):
        totalCombinedDF.loc[totalCombinedDF['cluster_predicted'] == num, ['Employer_Name']] = combinedClusters['Employer_Name'][num]
        totalCombinedDF.loc[totalCombinedDF['cluster_predicted'] == num, ['Job_Title']] = combinedClusters['Job_Title'][num]
        totalCombinedDF.loc[totalCombinedDF['cluster_predicted'] == num, ['SOC_Title']] = combinedClusters['SOC_Title'][num]

    totalCombinedDF = totalCombinedDF.drop(['cluster_predicted'], axis=1)
    totalCombinedDF.to_csv('output.csv', index=False)


# Extract the number of individuals that work at Google LLC and is a Software Developers, Applications
def c4(dataset):
    df = pd.DataFrame(dataset)
    df = df[df['Employer_Name'] == 'Google LLC']
    df = df[df['SOC_Title'] == 'Software Developers, Applications']
    print("The number of individuals that work at Google LLC and is a Software Developers, Applications: ", len(df))


# Extract the number of individuals that work at Google LLC and is a Software Developers, Applications
# And use laplace to achieve differential privacy
def c6(dataset):
    df = pd.DataFrame(dataset)
    df = df[df['Employer_Name'] == 'Google LLC']
    df = df[df['SOC_Title'] == 'Software Developers, Applications']
    value = len(df)

    rng = default_rng()
    sample = rng.laplace(loc=value, scale=4)

    sample = round(sample)
    print("The number of individuals that work at Google LLC and is a Software Developers, Applications (Differential Privacy): ", sample)


data = pd.read_csv('LCA_FY_2022.csv', nrows=2000)
c2(data)
c4(data)
c6(data)
