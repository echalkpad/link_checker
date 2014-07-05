ifdef NOLINT
    LINT=
else
    LINT=${GOPATH}/bin/golint .
endif

scraper: *.go
	go fmt .
	${LINT}
	go test
	go build .
