ifdef NOLINT
    LINT=
else
    LINT=${GOPATH}/bin/golint .
endif

scraper: *.go gen/scrapers.pb.go
	go get code.google.com/p/go.net/html
	go get code.google.com/p/go.net/html/atom
	go get github.com/Shopify/sarama
	go get gopkg.in/yaml.v2
	go get github.com/samuel/go-zookeeper/zk
	go get github.com/golang/lint/golint
	go fmt .
	go vet .
	${LINT}
	go test -parallel 5 -timeout 10s
	go build .

.PHONY: clean

clean:
	rm gen/*
	rm scraper

gen/scrapers.pb.go: ../proto/scrapers.proto
	protoc --go_out=gen/  -I../proto/ ../proto/*.proto

