package com.jamesmhare.beveragefinder.listeners;

import com.jamesmhare.beveragefinder.retrievers.DrinkRetriever;
import twitter4j.*;

import java.io.File;

public class TweetStreamListener {

    private static final Logger LOGGER = Logger.getLogger(TweetStreamListener.class);
    private Twitter twitter;

    StatusListener statusListener = new StatusListener() {
        @Override
        public void onStatus(Status status) {
            DrinkRetriever retriever = new DrinkRetriever();
            if (status.getUser().getScreenName().equals("BeverageFinder")) {
                LOGGER.info("This status was posted by @BeverageFinder.");
            } else if (status.getText().contains("RT")) {
                LOGGER.info("This is a retweet. Status will be ignored.");
            } else {
                try {
                    String searchString = status.getText().replace("@BeverageFinder", "").trim().replace(" ", "&");
                    String[] result = retriever.getDrink(searchString).split("::::");
                    if (result.length == 1) {
                        StatusUpdate drinkDoesNotExistStatusUpdate = new StatusUpdate(
                                "@" + status.getUser().getScreenName() + " "
                                        + result[0]
                        );
                        drinkDoesNotExistStatusUpdate.inReplyToStatusId(status.getId());
                        twitter.updateStatus(drinkDoesNotExistStatusUpdate);
                        LOGGER.info("Drink does not exist status update sent to " + status.getUser().getScreenName());
                    } else {
                        StatusUpdate statusUpdate1of2 = new StatusUpdate(
                                "@" + status.getUser().getScreenName() + " "
                                        + result[0]
                        );
                        if (result[2].equals("true")) {
                            statusUpdate1of2.setMedia(new File("tmp/images/DrinkImage.jpg"));
                        }
                        statusUpdate1of2.inReplyToStatusId(status.getId());
                        twitter.updateStatus(statusUpdate1of2);
                        LOGGER.info("Tweet 1 of 2 sent to " + status.getUser().getScreenName());

                        StatusUpdate statusUpdate2of2 = new StatusUpdate(
                                "@" + status.getUser().getScreenName() + " "
                                        + result[1]
                        );
                        if (result[2].equals("true")) {
                            statusUpdate2of2.setMedia(new File("tmp/images/DrinkImage.jpg"));
                        }
                        statusUpdate2of2.inReplyToStatusId(status.getId());
                        twitter.updateStatus(statusUpdate2of2);
                        LOGGER.info("Tweet 2 of 2 sent to " + status.getUser().getScreenName());

                        if (result[2].equals("true")) {
                            File tmpImageFile = new File("tmp/images/DrinkImage.jpg");
                            tmpImageFile.delete();
                        }
                    }
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
