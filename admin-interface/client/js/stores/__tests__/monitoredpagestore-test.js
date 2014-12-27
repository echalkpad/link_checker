jest.autoMockOff();

var constants = require('../../common/constants');
var Fluxxor = require('fluxxor');
var MonitoredPageStore = require('../monitoredpagestore');
var _ = require('underscore');

var newFlux = function() {
    var stores = {
      'MonitoredPageStore': new MonitoredPageStore()
    };

    var actions = {};

    return new Fluxxor.Flux(stores, actions);
};

describe('MonitoredPageStore', function() {
    var flux;
    var changesEmitted;
    var addsEmitted;
    var addsUrls;
    var deletesEmitted;
    var deletesUrls;
    var store;

    beforeEach(function() {
        flux = newFlux();
        changesEmitted = 0;
        addsEmitted = 0;
        addsUrls = [];
        deletesEmitted = 0;
        deletesUrls = [];
        store = flux.store("MonitoredPageStore");

        store.on('change', function () {
            changesEmitted++;
        });

        store.on(store.USER_ADDED_PAGE, function(url) {
            addsEmitted++;
            addsUrls.push(url);
        });

        store.on(store.USER_DELETED_PAGE, function(url) {
            deletesEmitted++;
            deletesUrls.push(url);
        });
    });

    it('can add a new page', function() {
        var URL = "http://www.foo.com/";

        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: URL}});

        expect(_.size(store.monitored_pages)).toEqual(1);

        expect(changesEmitted).toEqual(1);
        expect(addsEmitted).toEqual(1);
        expect(_.contains(addsUrls, URL)).toEqual(true);

        expect(store.monitored_pages.hasOwnProperty(URL)).toEqual(true);
        if (store.monitored_pages.hasOwnProperty(URL)) {
            expect(store.monitored_pages[URL].status).toEqual(constants.status.UNKNOWN);
            expect(store.monitored_pages[URL].sync_status).toEqual(constants.sync_status.ADD_SYNCING);
        }
    });

    it ('can delete a page', function() {
        var URL = "http://www.foo.com/";

        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: URL}});
        flux.dispatcher.dispatch({type: constants.DELETE_MONITORED_PAGE, payload: {url: URL}});
        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: URL}});

        expect(_.size(store.monitored_pages)).toEqual(1);

        expect(changesEmitted).toEqual(3);
        expect(addsEmitted).toEqual(2);
        expect(deletesEmitted).toEqual(1);

        expect(_.contains(addsUrls, URL)).toEqual(true);
        expect(_.contains(deletesUrls, URL)).toEqual(true);

        expect(store.monitored_pages.hasOwnProperty(URL)).toEqual(true);
        if (store.monitored_pages.hasOwnProperty(URL)) {
            expect(store.monitored_pages[URL].status).toEqual(constants.status.UNKNOWN);
            expect(store.monitored_pages[URL].sync_status).toEqual(constants.sync_status.ADD_SYNCING);
        }
    });

    it ('can process add sync updates', function() {
        var URL = 'http://www.foo.com/';

        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: URL}});
        flux.dispatcher.dispatch({type: constants.UPDATE_SERVER_SYNC_STATUS, payload: {url: URL, op: constants.ADD_MONITORED_PAGE}});

        expect(_.size(store.monitored_pages)).toEqual(1);
        expect(store.monitored_pages[URL].sync_status).toEqual(constants.sync_status.SYNCED);
    });

    it('will properly update status of a deleted page', function() {
        var URL = 'http://www.foo.com/';

        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: URL}});
        flux.dispatcher.dispatch({type: constants.DELETE_MONITORED_PAGE, payload: {url: URL}});

        flux.dispatcher.dispatch({type: constants.UPDATE_SERVER_SYNC_STATUS, payload: {url: URL, op: constants.ADD_MONITORED_PAGE}});
        flux.dispatcher.dispatch({type: constants.UPDATE_SERVER_SYNC_STATUS, payload: {url: URL, op: constants.DELETE_MONITORED_PAGE}});

        expect(_.size(store.monitored_pages)).toEqual(0);
    });


    it ('can process delete sync updates', function() {

    });

    it ('handles the sync update for add/delete/add case', function() {

    });

    it ('can reconcile server updates with the pending list', function() {

    });
});
