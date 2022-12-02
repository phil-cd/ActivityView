<h1 align="center">
  <img src="https://raw.githubusercontent.com/phil-cd/ActivityView/main/.github/images/ActivityView.png" width="620px"/><br/>
  ActivityView
</h1>

<p align="center">
GitHub-like ActivityView for android.
</p>

<p align="center">
<img src="https://github.com/phil-cd/ActivityView/workflows/Android%20CI/badge.svg?branch=main"/>
</p>

## Download

Get the AAR from GitHub Packages via Gradle:

```groovy
implementation 'de.philcd:activityview:<version>'
```

To be able to access the library you need to authenticate to GitHub Packages.
Therefore, add the GitHub Packages maven repository to your repositories list:

```groovy
maven {
    url 'https://maven.pkg.github.com/phil-cd/ActivityView'
    credentials {
        username = '<username>'
        password = '<access_token>'
    }
}
```

## Usage

### Simple Example

<img src="https://raw.githubusercontent.com/phil-cd/ActivityView/main/.github/images/ActivityViewRandom.png" width="400px"/><br/>

The following example code creates an activity view with random activities over the past 40 weeks.

```kotlin
// create map for activity values
val activities : MutableMap<Calendar, Int> = mutableMapOf()

// configure number of weeks that should be shown
val numWeeksToShow = 40

// generate some random activities
val currentCalendar = Calendar.getInstance()
currentCalendar.add(DAY_OF_YEAR, -7*numWeeksToShow)
while(currentCalendar < Calendar.getInstance()) {
    val activityCalendar = currentCalendar.clone() as Calendar
    val rand = Random().nextInt(10)
    activities[activityCalendar] = if (rand < 4) 0 else rand
    currentCalendar.add(DAY_OF_YEAR, 1)
}

// show activity view
ActivityView(activities = activities, numWeeksToShow = numWeeksToShow)
```

### Configuring Color

<img src="https://raw.githubusercontent.com/phil-cd/ActivityView/main/.github/images/ActivityViewBlue.png" width="400px"/><br/>

The color of activity items can be configured using `minColor` and `maxColor`.

```kotlin
val minColor = Color(185, 214, 245, 255)
val maxColor = Color(64, 139, 252, 255)
ActivityView(activities = activities, numWeeksToShow = numWeeksToShow, minColor = minColor, maxColor = maxColor)
```

The color for entries with no activity can be configured using `noActivityColor`.

### Additional Options

The shape of the activity entries and the font size of the labels can be configured using the `boxShape` and `monthLabelFontSize` arguments.
