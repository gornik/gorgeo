#!/usr/bin/env python

import os, sys, requests, time

DEFAULT_ES_HTTP = "http://localhost:9200"

def get_es_home():
    es_home = os.getenv("ES_HOME")
    if es_home == None:
        print >> sys.stderr, "Error: ES_HOME env variable not set"
        sys.exit(1)
    return es_home

def get_es_binary(es_home):
    if (es_home == None):
        es_home = get_es_home()
    es_bin = os.path.join(es_home, "bin/elasticsearch")
    if not os.path.exists(es_bin):
        print >> sys.stderr, "Error: elasticsearch script does not \
                exist in {0}".format(es_bin)
        sys.exit(1)
    return es_bin

def is_es_running(es_http):
    try:
        r = requests.head("http://localhost:9200")
        return r.status_code == requests.codes.ok
    except requests.exceptions.ConnectionError:
        return False

def retry_with_sleep(max_retries, condition):
    retries = 0 
    while not condition():
        retries = retries + 1
        if retries == max_retries:
            return False
        time.sleep(1)
    return True

def wait_for_es_start(es_http):
    if not retry_with_sleep(15, lambda: is_es_running(es_http)):
        print >> sys.stderr, "Error: elasticsearch did not start"
        sys.exit(1)

def wait_for_es_shutdown(es_http):
    if not retry_with_sleep(15, lambda: not is_es_running(es_http)):
        print >> sys.stderr, "Error: elasticsearch still running"
        sys.exit(1)

def start_es(es_home = None, es_http=DEFAULT_ES_HTTP):
    if not is_es_running(es_http):
        es_bin = get_es_binary(es_home) 
        os.system(es_bin)
        wait_for_es_start(es_http)
        print "Elasticsearch started"
    else:
        print "Elasticsearch already running"

def stop_es(es_http = DEFAULT_ES_HTTP):
    if is_es_running(es_http):
        requests.post(url="".join([es_http, "/_shutdown"]))
        wait_for_es_shutdown(es_http)
        print "Elasticsearch stopped"
    else:
        print "Elasticsearch already stopped"
