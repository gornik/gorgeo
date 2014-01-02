#!/usr/bin/env python

import es, sys

def main(args):
    es.start_es(args[0], args[1])

if __name__ == "__main__":
    main(sys.argv[1:])
