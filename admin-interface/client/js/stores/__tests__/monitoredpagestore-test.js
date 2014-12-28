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

var findByUrl = function(store, url) {
    return _.find(store, function(x) { return x.url === url;});
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


    it ('handles the sync update for add/delete/add case', function() {
        var URL = 'http://www.foo.com/';

        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: URL}});
        flux.dispatcher.dispatch({type: constants.DELETE_MONITORED_PAGE, payload: {url: URL}});
        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: URL}});

        flux.dispatcher.dispatch({type: constants.UPDATE_SERVER_SYNC_STATUS, payload: {url: URL, op: constants.ADD_MONITORED_PAGE}});
        flux.dispatcher.dispatch({type: constants.UPDATE_SERVER_SYNC_STATUS, payload: {url: URL, op: constants.DELETE_MONITORED_PAGE}});
        flux.dispatcher.dispatch({type: constants.UPDATE_SERVER_SYNC_STATUS, payload: {url: URL, op: constants.ADD_MONITORED_PAGE}});

        expect(_.size(store.monitored_pages)).toEqual(1);
        expect(store.monitored_pages[URL].sync_status).toEqual(constants.sync_status.SYNCED);
    });

    it ('can toggle expanded state to on', function() {
        var URL = 'http://www.foo.com';

        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: URL}});
        flux.dispatcher.dispatch({type: constants.TOGGLE_MONITORED_PAGE_EXPANDED_VIEW, payload: {url: URL}});

        expect(changesEmitted).toEqual(2);
        expect(store.monitored_pages[URL].expanded).toEqual(true);
    });

    it ('can toggle expanded state to off', function() {
        var URL = 'http://www.foo.com';

        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: URL}});
        flux.dispatcher.dispatch({type: constants.TOGGLE_MONITORED_PAGE_EXPANDED_VIEW, payload: {url: URL}});
        flux.dispatcher.dispatch({type: constants.TOGGLE_MONITORED_PAGE_EXPANDED_VIEW, payload: {url: URL}});

        expect(changesEmitted).toEqual(3);
        expect(store.monitored_pages[URL].expanded).toEqual(false);
    });

    it ('keeps expanded status the same when pulling down an update', function() {
        var expandedURL = 'http://www.expanded.com';
        var unexpandedURL = 'http://www.unexpanded.com';

        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: expandedURL}});
        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: unexpandedURL}});
        flux.dispatcher.dispatch({type: constants.UPDATE_SERVER_SYNC_STATUS, payload: {url: expandedURL, op: constants.ADD_MONITORED_PAGE}});
        flux.dispatcher.dispatch({type: constants.UPDATE_SERVER_SYNC_STATUS, payload: {url: unexpandedURL, op: constants.ADD_MONITORED_PAGE}});

        flux.dispatcher.dispatch({type: constants.TOGGLE_MONITORED_PAGE_EXPANDED_VIEW, payload: {url: expandedURL}});

        var serverURLs = [
            {url: expandedURL, status: constants.status.GOOD},
            {url: unexpandedURL, status: constants.status.GOOD}
        ];

        flux.dispatcher.dispatch({type: constants.UPDATE_MONITORED_PAGES_FROM_SERVER, payload: serverURLs});

        expect(store.monitored_pages.hasOwnProperty(expandedURL)).toEqual(true);
        expect(store.monitored_pages[expandedURL].expanded).toEqual(true);

        expect(store.monitored_pages.hasOwnProperty(unexpandedURL)).toEqual(true);
        expect(store.monitored_pages[unexpandedURL].expanded).toEqual(false);
    });

    it ('can reconcile server updates with the pending list', function() {
        var newURL = 'http://www.foo.com';

        var serverURLs = [
            {url: 'http://www.page1.com', status: constants.status.GOOD},
            {url: 'http://www.bad.com', stats: constants.status.BAD}
        ];

        var expectedURLs = [{url: newURL, status: constants.status.UNKNOWN}].concat(serverURLs);

        flux.dispatcher.dispatch({type: constants.ADD_MONITORED_PAGE, payload: {url: newURL}});
        flux.dispatcher.dispatch({type: constants.UPDATE_MONITORED_PAGES_FROM_SERVER, payload: serverURLs});

        expect(_.size(store.monitored_pages)).toEqual(_.size(serverURLs) + 1);

        expect(addsEmitted).toEqual(1);
        expectedURLs.forEach(function(expected) {
            var entry = findByUrl(store.monitored_pages, expected.url);
            // bit of a hack; the items we dispatched from the server have
            // status GOOD and BAD so we can use that to derive sync status
            var expectedSyncStatus = expected.status === constants.status.UNKNOWN ?
                                        constants.sync_status.ADD_SYNCING :
                                        constants.sync_status.SYNCED;
            expect(entry).toBeDefined();
            if (entry !== undefined) {
                expect(entry.url).toEqual(expected.url);
                expect(entry.status).toEqual(expected.status);
                expect(entry.sync_status).toEqual(expectedSyncStatus);
            }
        });
    });
});
