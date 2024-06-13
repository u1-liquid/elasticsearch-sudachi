import argparse
from multiprocessing import Pool
import urllib3.request
import json
from pathlib import Path

def parse_args():
    p = argparse.ArgumentParser()
    p.add_argument("--port", type=int, default=9200)
    p.add_argument("--host", default="http://localhost")
    p.add_argument("--docs", default=20000, type=int)
    p.add_argument("--index", default="test_sudachi")
    p.add_argument("--no-data", action="store_true")
    return p.parse_args()


def main(args):
    bulk_data = ""
    if args.no_data:
        bulk_data = create_bulk_data_nodata(args)
    else:
        bulk_data = create_bulk_data(args)
    
    es_instance = ElasticSearch(args)
    es_instance.put(bulk_data)
    es_instance.refresh()


def create_bulk_data_nodata(args):
    bulk_data = ""
    for i in range(args.docs):
        meta_data = {
            "index" : {
                "_index": args.index,
                "_id": i

            }
        }
        document_data = {
            "text": f"これはドキュメント＃{i}です"
        }
        bulk_data += json.dumps(meta_data, ensure_ascii=False) + "\n"
        bulk_data += json.dumps(document_data, ensure_ascii=False) + "\n"

    return bulk_data

def create_bulk_data(args):
    bulk_data = ""
    
    cur_dir = Path(__file__).parent
    with (cur_dir / "test-sentences.txt").open(encoding="utf-8") as inf:
        for i, line in enumerate(inf):
            if i >= args.docs:
                return bulk_data

            meta_data = {
                "index" : {
                    "_index": args.index,
                    "_id": i

                }
            }
            document_data = {
                "text": line.rstrip()
            }
            bulk_data += json.dumps(meta_data, ensure_ascii=False) + "\n"
            bulk_data += json.dumps(document_data, ensure_ascii=False) + "\n"

    return bulk_data




class ElasticSearch(object):
    def __init__(self, args):
        self.url = "{0}:{1}/{2}".format(args.host, args.port, args.index)
        self.count = 0
        self.mgr = urllib3.PoolManager()

    def put(self, bulk_data):
        url = f"{self.url}/_bulk"
        r = self.mgr.urlopen(
            "POST",
            url,
            headers={"Content-Type": "application/json"},
            body=bulk_data.encode('utf-8'),
        )
        if r.status != 200:
            raise Exception("Failed to POST")

        return

    def refresh(self):
        url = f"{self.url}/_refresh"
        return self.mgr.urlopen("POST", url).data


if __name__ == "__main__":
    main(parse_args())
