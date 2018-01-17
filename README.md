# tweetstats
Example application built as a non-blocking tweet parser using Scala's Akka Actor library  

Just run it at the sbt prompt and you will get a report of statistics every 5 seconds


## Usage
Add your consumer and access token as environment variables:

```bash
export TWITTER_CONSUMER_TOKEN_KEY='my-consumer-key'
export TWITTER_CONSUMER_TOKEN_SECRET='my-consumer-secret'
export TWITTER_ACCESS_TOKEN_KEY='my-access-key'
export TWITTER_ACCESS_TOKEN_SECRET='my-access-secret'
```

or in application.conf
```bash
twitter {
  consumer {
    key = "xxxx"
    secret = "xxx"
  }
  access {
    key = "xxxx"
    secret = "xxx"
  }
}
```


