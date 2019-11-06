# Fixed Length File Handler



## Introduction
When processing data from some systems (mainly legacy ones), it's usual to have Fixed Length Files, which are files that contain lines which content is split using a specific length for each field of a record.

This kind of files are sometimes tricky to handle as many times there is a spaghetti of string manipulations and padding, and character counting and...Well, many things to take care of.

This library comes to the rescue of programmers dealing with fixed length files. It enables you to simply define how your records are structured and it will handle these records for you in a nice Kotlin DSL for further processing.

## Using with Gradle

This library is published to `Bintray jcenter`, so you'll need to configure that in your repositories:
```kotlin
repositories {
    mavenCentral()
    jcenter()
}
```

And then you can import it into your dependencies:
```kotlin
dependencies {
    implementation("br.com.guiabolso:fixed-length-file-handler:{version}")
}
```

## Basic Usage

The basic usage assumes that you're reading a file with a single type of record.

Given a Fixed-Length File:


#### Definition

| Field | Type | Initial Position | Final Position Exclusive | 
| ----- | ---- | ---------------- | ------------------------ |
| UserName | String | 0 | 30 |
| User Document | Int | 30 | 39 |
| User Registry Date | LocalDate | 39 | 49 | 

#### File

```
FirstUsername                 1234567892019-02-09
SecondAndLongerUsername       9876543212018-03-10
ThirdUsernameWithShorterDoc   0000001232017-04-11
```

We can parse it with the `fixedLengthFileParser` DSL:

```kotlin
data class MyUserRecord(val username: String, val userDoc: Int, val registryDate: LocalDate)

val fileInputStream: InputStream = getFileInputStream()


fixedLengthFileParser<MyUserRecord>(fileInputStream) {
    MyUserRecord(
        field(0, 30, Padding.PaddingRight(' ')),
        field(30, 39, Padding.PaddingLeft('0')),
        field(39, 49)
    )    
}
```

The library is prepared to handle `Left Padding` and `Right Padding`. It's also prepared to handle many of Kotlin/Java types.

## Default parsing

This library is prepared to handle some of the most usual Kotlin/Java types. More types may be added if they're required. The default types are:

- String
- Int
- Double
- Long
- Char
- Boolean (Case insensitive)
- LocalDate (Using default DateTimeFormatter)
- LocalDateTime (Using default DateTimeFormatter)
- BigDecimal

## Custom parsing

There might be times where the default types are not enough, and you need a custom parser for a given record.

For example: You know that a specific number contains a currency, and the last two digits are used for the cents.

This library is prepared to handle cases where you need custom parsing for a String, by modifying the `field` invocation:

```kotlin

// Parsing the field 0000520099 to 5200.99 

field(15, 25, Padding.PaddingLeft('0')) { str: String -> StringBuilder(str).insert(str.length - 2, ".").toString().toBigDecimal() }
``` 

## Advanced Usage

For a unknown reason, many Fixed-Length file providers use the same file for more than one record, denoting a specific bit for record identification, so there's a possibility that this happens:

```
1 FirstUserName       123.12
1 SecondUserName      002.50
2 123456789     2019-02-09UserDocs
2 000812347     2018-03-08AnotherUserDocs
```

In this cases, the software must look at the first `char` to determine the record type. This situation is usually what leads to a spaghetti string manipulation. We can solve it by using this library's "advanced" options:

```kotlin
data class FirstRecordType(username: String, userMoney: BigDecimal)
data class SecondRecordType(userCode: Int, registerDate: LocalDate, docs: String)

fixedLengthFileParser(fileInputStream) {
    withRecord({ line -> line[0] == '1' }) {
        FirstRecordType(
            field(2, 22, Padding.PaddingRight(' ')),
            field(22, 28, Padding.PaddingLeft('0'))
        )
    }
    
    withRecord( { line -> line[0] == '2' }) {
        SecondRecordType(
            field(2, 15, Padding.PaddingRight(' ')),
            field(15, 25),
            field(25, 40, Padding.PaddingRight(' '))
        )
    }
}
```

## Features

- The file is streamed into a sequence of values, and is never loaded in its entirety to the memory. You should expect this to have a good performance over a very big file.
- The Kotlin DSL makes it easier to define the file parsing in a single point, and de sequence processing can be done anywhere