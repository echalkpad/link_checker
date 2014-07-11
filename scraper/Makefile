ifdef NOLINT
    LINT=
else
    LINT=${GOPATH}/bin/golint .
endif

scraper: *.go
	go get code.google.com/p/go.net/html
	go get code.google.com/p/go.net/html/atom
	go fmt .
	${LINT}
	go test -parallel 5 -timeout 10s
	go build .
