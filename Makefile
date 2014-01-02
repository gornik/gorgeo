


ES_HOME = /opt/elasticsearch-0.90.9
ES_HTTP = http://localhost:9200

MVN = mvn
PYTHON = python

CURRDIR = $(shell pwd)
PLUGIN = $(shell find "target/release" -name "gorgeo*.zip")
PLUGIN_URL = "file://$(CURRDIR)/$(PLUGIN)"

.PHONY: all clean compile package es_start es_stop es_restart install uninstall

all: package

clean:
	@$(MVN) clean

compile: clean
	@$(MVN) compile

package: compile
	@$(MVN) package

es_start:
	./utils/start_es.py $(ES_HOME) $(ES_HTTP)

es_stop:
	./utils/stop_es.py $(ES_HTTP)

es_restart: es_stop es_start

install: uninstall
	@$(ES_HOME)/bin/plugin --install gorgeo --url $(PLUGIN_URL)
	${MAKE} es_restart

uninstall:
	$(ES_HOME)/bin/plugin --remove gorgeo
