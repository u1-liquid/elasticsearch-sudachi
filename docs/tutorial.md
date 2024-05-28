# Elasticsearch / OpenSearch Sudachi プラグイン チュートリアル

elasticsearch-sudachi プラグインは Elasticsearch 7.17, 7.10 の最新バージョンと 8 系、および OpenSearch 2.6 以降をサポートしています。

以下では Elasticsearch 8.13.4 で Sudachi をつかう手順をしめします。
OpenSearch の場合も同様の手順になります。

Elasticsearch をインストールします。

```
$ wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.13.4-linux-x86_64.tar.gz
$ tar xzf elasticsearch-8.13.4-linux-x86_64.tar.gz
```

プラグインをインストールします。

```
$ cd elasticsearch-8.13.4/
$ bin/elasticsearch-plugin install https://github.com/WorksApplications/elasticsearch-sudachi/releases/download/v3.1.1/elasticsearch-8.13.4-analysis-sudachi-3.1.1.zip
```

パッケージには辞書が含まれていません。https://github.com/WorksApplications/SudachiDict から最新の辞書を取得し、 `es_config/sudachi` の下に置きます。 3 つの辞書のうち以下では core 辞書を利用します。

```
$ wget https://d2ej7fkh96fzlu.cloudfront.net/sudachidict/sudachi-dictionary-latest-core.zip
$ unzip sudachi-dictionary-latest-core.zip
$ mkdir config/sudachi
$ cp sudachi-dictionary-*/system_core.dic config/sudachi/system_core.dic
```

配置後、Elasticsearch を起動します。

```
$ bin/elasticsearch
```

設定ファイルを作成します。

```json:analysis_sudachi.json
{
    "settings" : {
        "analysis" : {
            "filter" : {
                "search" : {
                    "type" : "sudachi_split",
                    "mode" : "search"
                },
                "synonym" : {
                    "type" : "synonym",
                    "synonyms" : [ "関西国際空港,関空", "関西 => 近畿" ]
                },
                "romaji_readingform" : {
                    "type" : "sudachi_readingform",
                    "use_romaji" : true
                },
                "katakana_readingform" : {
                    "type" : "sudachi_readingform",
                    "use_romaji" : false
                }
            },
            "analyzer" : {
                "sudachi_baseform_analyzer" : {
                    "filter" : [ "sudachi_baseform" ],
                    "type" : "custom",
                    "tokenizer" : "sudachi_tokenizer"
                },
                "sudachi_normalizedform_analyzer" : {
                    "filter" : [ "sudachi_normalizedform" ],
                    "type" : "custom",
                    "tokenizer" : "sudachi_tokenizer"
                },
                "sudachi_readingform_analyzer" : {
                    "filter" : [ "katakana_readingform" ],
                    "type" : "custom",
                    "tokenizer" : "sudachi_tokenizer"
                },
                "sudachi_romaji_analyzer" : {
                    "filter" : [ "romaji_readingform" ],
                    "type" : "custom",
                    "tokenizer" : "sudachi_tokenizer"
                },
                "sudachi_search_analyzer" : {
                    "filter" : [ "search" ],
                    "type" : "custom",
                    "tokenizer" : "sudachi_tokenizer"
                },
                "sudachi_synonym_analyzer" : {
                    "filter" : [ "synonym", "search" ],
                    "type" : "custom",
                    "tokenizer" : "sudachi_tokenizer"
                },
                "sudachi_analyzer" : {
                    "filter" : [],
                    "type": "custom",
                    "tokenizer": "sudachi_tokenizer"
                },
                "sudachi_a_analyzer" : {
                    "filter" : [],
                    "type" : "custom",
                    "tokenizer" : "sudachi_a_tokenizer"
                }
            },
            "tokenizer" : {
                "sudachi_tokenizer": {
                    "type": "sudachi_tokenizer",
                    "split_mode": "C"
                },
                "sudachi_a_tokenizer": {
                    "type": "sudachi_tokenizer",
                    "split_mode": "A"
                }
            }
        }
    }
}
```

インデックスを作成します。

```
$ curl -X PUT 'localhost:9200/test_sudachi' -H 'Content-Type: application/json' -d @analysis_sudachi.json
{"acknowledged":true,"shards_acknowledged":true,"index":"test_sudachi"}
```

解析してみます。

```
$ curl -X GET "localhost:9200/test_sudachi/_analyze?pretty" -H 'Content-Type: application/json' -d'{"analyzer":"sudachi_analyzer", "text" : "関西国際空港"}'
{
  "tokens" : [
    {
      "token" : "関西国際空港",
      "start_offset" : 0,
      "end_offset" : 6,
      "type" : "word",
      "position" : 0
    }
  ]
}
```

C 単位で分割されます。

A 単位で分割すると以下のようになります。

```
$ curl -X GET "localhost:9200/test_sudachi/_analyze?pretty" -H 'Content-Type: application/json' -d'{"analyzer":"sudachi_a_analyzer", "text" : "関西国際空港"}'
{
  "tokens" : [
    {
      "token" : "関西",
      "start_offset" : 0,
      "end_offset" : 2,
      "type" : "word",
      "position" : 0
    },
    {
      "token" : "国際",
      "start_offset" : 2,
      "end_offset" : 4,
      "type" : "word",
      "position" : 1
    },
    {
      "token" : "空港",
      "start_offset" : 4,
      "end_offset" : 6,
      "type" : "word",
      "position" : 2
    }
  ]
}
```

動詞、形容詞を終止形で出力してみます。

```
$ curl -X GET "localhost:9200/test_sudachi/_analyze?pretty" -H 'Content-Type: application/json' -d'{"analyzer":"sudachi_baseform_analyzer", "text" : "おおきく"}'
{
  "tokens" : [
    {
      "token" : "おおきい",
      "start_offset" : 0,
      "end_offset" : 4,
      "type" : "word",
      "position" : 0
    }
  ]
}
```

表記を正規化して出力してみます。

```
$ curl -X GET "localhost:9200/test_sudachi/_analyze?pretty" -H 'Content-Type: application/json' -d'{"analyzer":"sudachi_normalizedform_analyzer", "text" : "おおきく"}'
{
  "tokens" : [
    {
      "token" : "大きい",
      "start_offset" : 0,
      "end_offset" : 4,
      "type" : "word",
      "position" : 0
    }
  ]
}
```

読みを出力してみます。

```
$ curl -X GET "localhost:9200/test_sudachi/_analyze?pretty" -H 'Content-Type: application/json' -d'{"analyzer":"sudachi_readingform_analyzer", "text" : "おおきく"}'
{
  "tokens" : [
    {
      "token" : "オオキク",
      "start_offset" : 0,
      "end_offset" : 4,
      "type" : "word",
      "position" : 0
    }
  ]
}
```

読みをローマ字 (Microsoft IME 風) で出力してみます。

```
$ curl -X GET "localhost:9200/test_sudachi/_analyze?pretty" -H 'Content-Type: application/json' -d'{"analyzer":"sudachi_romaji_analyzer", "text" : "おおきく"}'
{
  "tokens" : [
    {
      "token" : "ookiku",
      "start_offset" : 0,
      "end_offset" : 4,
      "type" : "word",
      "position" : 0
    }
  ]
}
```

そのほか、品詞によるトークンの除外、ストップワードなどが利用できます。

kuromoji の `search` モードや `extended` モードと同様の動作をさせたいときは `sudachi_split` フィルターをつかいます。

```
$ curl -X GET "localhost:9200/test_sudachi/_analyze?pretty" -H 'Content-Type: application/json' -d'{"analyzer":"sudachi_search_analyzer", "text" : "関西国際空港"}'
{
  "tokens" : [
    {
      "token" : "関西国際空港",
      "start_offset" : 0,
      "end_offset" : 6,
      "type" : "word",
      "position" : 0,
      "positionLength" : 3
    },
    {
      "token" : "関西",
      "start_offset" : 0,
      "end_offset" : 2,
      "type" : "word",
      "position" : 0
    },
    {
      "token" : "国際",
      "start_offset" : 2,
      "end_offset" : 4,
      "type" : "word",
      "position" : 1
    },
    {
      "token" : "空港",
      "start_offset" : 4,
      "end_offset" : 6,
      "type" : "word",
      "position" : 2
    }
  ]
}
```

同義語展開と組み合わせることもできます。

```
$ curl -X GET "localhost:9200/test_sudachi/_analyze?pretty" -H 'Content-Type: application/json' -d'{"analyzer":"sudachi_synonym_analyzer", "text" : "関西国際空港と関西"}'
{
  "tokens" : [
    {
      "token" : "関西国際空港",
      "start_offset" : 0,
      "end_offset" : 6,
      "type" : "word",
      "position" : 0,
      "positionLength" : 3
    },
    {
      "token" : "関西",
      "start_offset" : 0,
      "end_offset" : 2,
      "type" : "word",
      "position" : 0
    },
    {
      "token" : "国際",
      "start_offset" : 2,
      "end_offset" : 4,
      "type" : "word",
      "position" : 1
    },
    {
      "token" : "空港",
      "start_offset" : 4,
      "end_offset" : 6,
      "type" : "word",
      "position" : 2
    },
    {
      "token" : "関空",
      "start_offset" : 0,
      "end_offset" : 6,
      "type" : "SYNONYM",
      "position" : 2
    },
    {
      "token" : "と",
      "start_offset" : 6,
      "end_offset" : 7,
      "type" : "word",
      "position" : 3
    },
    {
      "token" : "近畿",
      "start_offset" : 7,
      "end_offset" : 9,
      "type" : "SYNONYM",
      "position" : 4
    }
  ]
}
```

こちらもご参照ください:

- [Elasticsearch のための新しい形態素解析器 「Sudachi」 - Qiita](https://qiita.com/sorami/items/99604ef105f13d2d472b) （Elastic stack Advent Calendar 2017）
- [OpenSearch + Sudachi を 0 から構築する - Qiita](https://qiita.com/mh-northlander/items/83c3888bb5fefe34be20)
