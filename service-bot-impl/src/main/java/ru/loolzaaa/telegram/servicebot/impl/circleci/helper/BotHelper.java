package ru.loolzaaa.telegram.servicebot.impl.circleci.helper;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.loolzaaa.telegram.servicebot.core.bot.config.BotConfiguration;
import ru.loolzaaa.telegram.servicebot.core.command.ClearConfigCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.command.CircleCICommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.command.CircleCIResultCommand;
import ru.loolzaaa.telegram.servicebot.impl.circleci.config.user.BotUser;

import java.util.List;

/**
 * Helper class for CircleCI Telegram Bot implementation
 *
 * @author Andrey Korsakov (loolzaaa)
 */
public class BotHelper {

    /**
     * Replace message text in Message object to command text.
     * Also, add MessageEntity with command to Message object.
     *
     * @param update An object to update message
     * @param command A full qualified string bot command
     */
    public static void changeMessageToCommand(Update update, final String command) {
        MessageEntity messageEntity = new MessageEntity(EntityType.BOTCOMMAND, 0, command.length());
        update.getMessage().setText(command);
        update.getMessage().setEntities(List.of(messageEntity));
    }

    /**
     * Helper method that register all standard commands for this
     * bot implementation
     *
     * @param bot bot instance for command registry
     * @param configuration bot configuration for registering commands
     */
    public static void registerAllDefaultCommands(ICommandRegistry bot, BotConfiguration<BotUser> configuration) {
        bot.register(new ClearConfigCommand<>("clear_config", "Clear bot configuration", configuration));
        bot.register(new CircleCIResultCommand("circleci_result", "CircleCI Webhook", configuration));
        bot.register(new CircleCICommand("circleci", "CircleCI API", configuration));
    }
}
