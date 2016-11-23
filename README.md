# TP_Astro_MSBGD
---
A correction of the Spark's practical work project for MSBGD 2017: Classification of Exoplanets.

## Introduction
**Goal**  : Create a Binary classifier for exoplanets.

**Context** : Exoplanets are planets rotating around other stars than the Sun. Their study gives us a better understanding of the formation of the solar system. Some of them could possibly shelter forms of life.


They are detected in two steps:
* A *Satellite* (Kepler) observes the stars and marks those whose luminosity curve shows a "hollow", which could indicate that a planet has passed (part of the light emitted by the star being obscured by the passage of the planet). This method of "transit" allows us to define candidate exoplanets, and to deduce the characteristics that the planet would have if it really existed (distance to its star, diameter, shape of its orbit, etc.).
* It is then necessary to validate or invalidate the candidates using another more expensive method, based on measurements of radial velocities of the star. Candidates are then classified as "confirmed" or "false-positive".

As there are about 200 billion stars in our galaxy, and therefore potentially as much (or even more) exoplanets, their detection must be automated to "scale up". The method of transits is already automatic (more than 22 million curves of luminosity recorded by Kepler), but not the confirmation of the candidate planets, hence the automatic classifier that we will build.

## Data
Data on exoplanets is public and available online (check the [link](http://exoplanetarchive.ipac.caltech.edu/index.html)). There are already 3388 confirmed exoplanets and about as many false positives, our classifier will be trained on this data. There is one observation per line. The column "koi_disposition" represents the labels. 
A clean dataset is provided with this correction.

## About the classifier
We need a binary classifier (we have two classes of observations: validated, invalidated).
We chose to use a logistic regression with a regularisation using the L1 norm of the vector defining the separating hyperplane between the two classes (LASSO).
([Link for the implementation with Spark Ml](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.ml.classification.LogisticRegression)) method with L1 regularization.

A grid search is also used to find the best regularization parameter to solve this particular problem.

## How to use this repository

**Build the project** :
```
sbt assembly
```

**Running the project**

```
/path/to/spark/bin/spark-submit \
--class com.sparkProject.Job \
--master local \
/path/to/local/jar/file/correctionTPSpark-assembly-1.0.jar \
-i /input/path/for/the/data \
-m /path/for/the/saved/model
```

Open localhost:4040 in your browser to follow the job in SparkUI.
In chrome you can install an auto refresher plugin to update the page.