package com.roylenferink.bitbucket.hook;

import com.atlassian.bitbucket.commit.Changeset;
import com.atlassian.bitbucket.commit.ChangesetsRequest;
import com.atlassian.bitbucket.commit.CommitService;
import com.atlassian.bitbucket.content.*;
import com.atlassian.bitbucket.hook.repository.PostRepositoryHook;
import com.atlassian.bitbucket.hook.repository.PostRepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.PullRequestMergeHookRequest;
import com.atlassian.bitbucket.io.TypeAwareOutputSupplier;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.roylenferink.bitbucket.CopyrightSettingsHelper;
import com.roylenferink.bitbucket.logger.PluginLoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.time.Year;
import java.util.HashSet;
import java.util.Set;

public class UpdateCopyrightHook implements PostRepositoryHook<PullRequestMergeHookRequest> {

    private final Logger log;
    private final CommitService commitService;
    private final ContentService contentService;

    public UpdateCopyrightHook(@ComponentImport final CommitService commitService,
                               @ComponentImport final ContentService contentService) {
        PluginLoggerFactory lf = PluginLoggerFactory.getInstance();
        this.log =  lf.getLoggerForThis(this);

        this.commitService = commitService;
        this.contentService = contentService;
    }

    @Override
    public void postUpdate(@Nonnull PostRepositoryHookContext context, @Nonnull PullRequestMergeHookRequest request) {
        if (request.isDryRun())
            return; // Don't do anything if a dryrun is being executed

        if (!request.getMergeHash().isPresent())
            return; // No merge commit available

        String copyrightRegex = CopyrightSettingsHelper.getCopyrightRegexSettingRaw(context.getSettings());
        if (copyrightRegex == null) {
            log.error("No regular expression configured to use for checking copyright headers. Configure one in the project/repository settings!");
            return;
        }

        long startTime = System.currentTimeMillis();
        String mergeCommit = request.getMergeHash().get();

        log.debug("Pull request in [{}] merged from [{}] to [{}] in commit [{}]",
                request.getRepository(),
                request.getFromRef().getId(),
                request.getToRef().getId(),
                mergeCommit);

        ChangesetsRequest csr = new ChangesetsRequest.Builder(request.getRepository())
                                    .commitId(mergeCommit)
                                    .build();

        Set<Path> binaryFiles = new HashSet<>();
        DiffRequest.Builder diffRequestBuilder = new DiffRequest.Builder(request.getRepository(), mergeCommit);
        commitService.streamDiff(diffRequestBuilder.build(), new AbstractDiffContentCallback() {
            @Override
            public void onBinary(@Nullable Path src, @Nullable Path dst) throws IOException {
                super.onBinary(src, dst);
                binaryFiles.add(src);
                binaryFiles.add(dst);
            }
        });

        Page<Changeset> changeSets = commitService.getChangesets(csr, new PageRequestImpl(0, 9999));
        Set<Path> changedFiles = new HashSet<>();
        for (Changeset cs : changeSets.getValues()) {
            Page<Change> changes = cs.getChanges();
            for (Change c : changes.getValues()) {
                if (c.getNodeType() == ContentTreeNode.Type.FILE
                        && c.getType() != ChangeType.DELETE
                        && !binaryFiles.contains(c.getPath()) ) {
                    changedFiles.add(c.getPath());
                }
            }
        }

        for (Path p : changedFiles) {
            log.debug("Text file changed in PR: {}", p.toString());

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            contentService.streamFile(request.getRepository(), mergeCommit, p.toString(), new TypeAwareOutputSupplier() {
                @Override
                @Nonnull
                public OutputStream getStream(@Nonnull String s) throws IOException {
                    return os;
                }
            });

            try {
                String fileContent = new String(os.toByteArray(), "UTF-8");

//                String textToProcess;
//                if (fileContent.length() > maxCharactersToProcess) {
//                    textToProcess = fileContent.substring(0, maxCharactersToProcess);
//                } else {
//                    textToProcess = fileContent;
//                }

                String updatedContent = fileContent.replaceAll(copyrightRegex, "$1 " + Year.now().getValue() + " $2");

                //TODO: Write updatedContent to file and create new commit
            } catch (UnsupportedEncodingException e) {
                log.error("Error parsing file content: {}", e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        log.trace("Time spent on {} in {}: {} ms", mergeCommit, request.getRepository(), (endTime - startTime));
    }

}
