package com.roylenferink.bitbucket.hook;

import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.event.api.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens for merged pull requests to trigger a copyright update
 *
 * @author Roy Lenferink
 */
public class PullRequestListener {

    private final Logger rootLogger = LoggerFactory.getLogger(PullRequestListener.class);

    public PullRequestListener() {
        rootLogger.info("PullRequestListener created");
    }

    // This event signifies that s PR has been merged
    @EventListener
    public void listenForMerged(PullRequestMergedEvent event) {
        final PullRequest pr = event.getPullRequest();
        final Repository repo = pr.getToRef().getRepository();

        rootLogger.info("Repo name: " + repo.getName());
        rootLogger.info("Repo slug: " + repo.getSlug());
        rootLogger.info("Repo SCM ID: " + repo.getScmId());
        rootLogger.info("PR from: " + pr.getFromRef());
        rootLogger.info("PR to: " + pr.getFromRef());

//        // just trigger a build of the new commit since the other hook doesn't catch merged PRs.
//        String mergeSha1 = event.
//        String targetBranch = pr.getToRef().getId();

    }

}
