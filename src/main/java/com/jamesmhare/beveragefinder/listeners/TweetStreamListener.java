package com.jamesmhare.beveragefinder.listeners;

import com.jamesmhare.beveragefinder.retrievers.RecipeRetriever;
import twitter4j.*;

public class TweetStreamListener {

    private static final Logger LOGGER = Logger.getLogger(TweetStreamListener.class);
    private Twitter twitter;
    private RecipeRetriever retriever = new RecipeRetriever();

    StatusListener statusListener = new StatusListener() {
        @Override
        public void onStatus(Status status) {
            if (status.getUser().getScreenName().equals("BeverageFinder")) {
                LOGGER.info("This status was posted by @BeverageFinder.");
            } else {
                try {
                    String searchString = status.getText().replace("@BeverageFinder", "").trim().replace(" ", "&");
                    StatusUpdate statusUpdate = new StatusUpdate(
                            "@" + status.getUser().getScreenName() + " "
                                    + retriever.getRecipe(searchString)
                    );
                    statusUpdate.inReplyToStatusId(status.getId());
                    twitter.updateStatus(statusUpdate);
                } catch (TwitterException exception) {
                    LOGGER.error(exception.getMessage());
                }
            }
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            LOGGER.info("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            LOGGER.info("Got track limitation notice:" + numberOfLimitedStatuses);
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
            LOGGER.info("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
        }

        @Override
        public void onStallWarning(StallWarning warning) {
            LOGGER.warn("Got stall warning:" + warning);
        }

        @Override
        public void onException(Exception exception) {
            LOGGER.error(exception.getMessage());
        }
    };

    public TweetStreamListener(Twitter twitter) {
        this.twitter = twitter;
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(statusListener);
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.follow(1122613190993555458L);
        twitterStream.filter(filterQuery);
    }

}
