#!/usr/bin/env python

import es, sys

def main(args):
    es.stop_es(args[0])

if __name__ == "__main__":
    main(sys.argv[1:])
