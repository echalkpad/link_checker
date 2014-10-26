package main

import (
	"testing"
	"time"
)

type TestMessage struct {
	msg string
}

type TestProcessor struct {
	messageLogged string
}

func (t *TestProcessor) Process(msg interface{}) {
	testMessage := msg.(*TestMessage)
	t.messageLogged = testMessage.msg
}

func TestWebProcessor(t *testing.T) {
	testProcessor := &TestProcessor{}
	mp := NewQueueProcessor(testProcessor)
	testConst := "TESTING"

	mp.Start()
	mp.Process(&TestMessage{msg: testConst})
	time.Sleep(100 * time.Millisecond)
	mp.Stop()

	if testProcessor.messageLogged != testConst {
		t.Errorf("Expected tp messageLogged to be %s, was %s", testConst, testProcessor.messageLogged)
	}
}
