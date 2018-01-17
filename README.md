# tweetstats
Example application built as a non-blocking tweet parser using Scala's Akka Actor library  


## Usage
Add your consumer and access token as environment variables:

```bash
export TWITTER_CONSUMER_TOKEN_KEY='my-consumer-key'
export TWITTER_CONSUMER_TOKEN_SECRET='my-consumer-secret'
export TWITTER_ACCESS_TOKEN_KEY='my-access-key'
export TWITTER_ACCESS_TOKEN_SECRET='my-access-secret'
```

or in application.conf
,,,
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
'''


