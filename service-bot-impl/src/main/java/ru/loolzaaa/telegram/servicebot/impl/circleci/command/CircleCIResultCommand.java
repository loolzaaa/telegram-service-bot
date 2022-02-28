package ru.loolzaaa.telegram.servicebot.impl.circleci.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.CommonCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.Subscription;
import ru.loolzaaa.telegram.servicebot.impl.circleci.helper.ResultHelper;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

public class CircleCIResultCommand extends CommonCommand<BotUser> {

    private static final String CIRCLECI_RESULT_KEY = System.getenv("circleci_resultKey");

    public CircleCIResultCommand(String commandIdentifier, String description, BotConfiguration<BotUser> configuration) {
        super(commandIdentifier, description, configuration);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        if (arguments.length >= 10 && arguments[0].equals(CIRCLECI_RESULT_KEY)) {
            String type = arguments[1];
            String workflowStatus = arguments[2];
            String commitAuthor = arguments[3];
            String projectSlug = arguments[4];
            String workflowName = arguments[5];
            String branchName = arguments[6];
            String commitName = arguments[7];
            String commitHash = arguments[8];
            String jobName = arguments[9];
            if (EventType.WORKFLOW_COMPLETED.getType().equals(type)) {
                if (projectSlug.toLowerCase().startsWith("gh")) projectSlug = "github" + projectSlug.substring(2);
                if (projectSlug.toLowerCase().startsWith("bb")) projectSlug = "bitbucket" + projectSlug.substring(2);

                ResultHelper.ResultData resultData = new ResultHelper.ResultData(commitAuthor, projectSlug, workflowName,
                        branchName, commitName, commitHash, jobName);
                byte[] resultImage = "success".equals(workflowStatus) ?
                        ResultHelper.getResult(ResultHelper.Result.SUCCESS, resultData) :
                        "failed".equals(workflowStatus) ?
                                ResultHelper.getResult(ResultHelper.Result.FAILED, resultData) :
                                null;
                InputFile file = resultImage == null ?
                        null :
                        new InputFile(new ByteArrayInputStream(resultImage), "result.png");

                final String conditionSlug = projectSlug;
                List<BotUser> users = configuration.getUsers().stream()
                        .filter(u -> {
                            List<Subscription> subscriptions = u.getSubscriptions();
                            List<String> slugs = subscriptions.stream()
                                    .filter(subscription -> subscription.getSlug() != null)
                                    .map(subscription -> subscription.getSlug().toLowerCase())
                                    .collect(Collectors.toList());
                            return slugs.contains(conditionSlug.toLowerCase());
                        })
                        .collect(Collectors.toList());
                for (BotUser u : users) {
                    try {
                        if (file != null) {
                            SendPhoto answer = SendPhoto.builder()
                                    .chatId(u.getChatId().toString())
                                    .photo(file)
                                    .build();
                            absSender.execute(answer);
                        } else {
                            SendMessage answer = SendMessage.builder()
                                    .chatId(chat.getId().toString())
                                    .text(projectSlug + "--" + workflowStatus)
                                    .build();
                            sendAnswer(absSender, answer);
                        }
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //TODO: add other type support
            }
        }
    }
}
