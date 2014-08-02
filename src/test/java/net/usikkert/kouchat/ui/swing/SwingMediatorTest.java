
/***************************************************************************
 *   Copyright 2006-2014 by Christian Ihle                                 *
 *   contact@kouchat.net                                                   *
 *                                                                         *
 *   This file is part of KouChat.                                         *
 *                                                                         *
 *   KouChat is free software; you can redistribute it and/or modify       *
 *   it under the terms of the GNU Lesser General Public License as        *
 *   published by the Free Software Foundation, either version 3 of        *
 *   the License, or (at your option) any later version.                   *
 *                                                                         *
 *   KouChat is distributed in the hope that it will be useful,            *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU      *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with KouChat.                                           *
 *   If not, see <http://www.gnu.org/licenses/>.                           *
 ***************************************************************************/

package net.usikkert.kouchat.ui.swing;

import static org.mockito.Mockito.*;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.usikkert.kouchat.jmx.JMXAgent;
import net.usikkert.kouchat.message.Messages;
import net.usikkert.kouchat.message.PropertyFileMessages;
import net.usikkert.kouchat.misc.CommandException;
import net.usikkert.kouchat.misc.CommandParser;
import net.usikkert.kouchat.misc.Controller;
import net.usikkert.kouchat.misc.MessageController;
import net.usikkert.kouchat.misc.Settings;
import net.usikkert.kouchat.misc.SortedUserList;
import net.usikkert.kouchat.misc.Topic;
import net.usikkert.kouchat.misc.User;
import net.usikkert.kouchat.ui.swing.settings.SettingsDialog;
import net.usikkert.kouchat.util.TestUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;

/**
 * Test of {@link SwingMediator}.
 *
 * @author Christian Ihle
 */
@SuppressWarnings("HardCodedStringLiteral")
public class SwingMediatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public ExpectedSystemExit expectedSystemExit = ExpectedSystemExit.none();

    private SwingMediator mediator;

    private User me;
    private JTextField messageTF;
    private UITools uiTools;
    private Controller controller;
    private JMXAgent jmxAgent;
    private CommandParser cmdParser;
    private ComponentHandler componentHandler;
    private KouChatFrame kouChatFrame;
    private SysTray sysTray;

    @Before
    public void setUp() {
        messageTF = mock(JTextField.class);

        final MainPanel mainPanel = mock(MainPanel.class);
        when(mainPanel.getMsgTF()).thenReturn(messageTF);

        kouChatFrame = mock(KouChatFrame.class);
        sysTray = mock(SysTray.class);

        componentHandler = spy(new ComponentHandler());
        componentHandler.setButtonPanel(mock(ButtonPanel.class));
        componentHandler.setGui(kouChatFrame);
        componentHandler.setMainPanel(mainPanel);
        componentHandler.setMenuBar(mock(MenuBar.class));
        componentHandler.setSettingsDialog(mock(SettingsDialog.class));
        componentHandler.setSidePanel(mock(SidePanel.class));
        componentHandler.setSysTray(sysTray);

        me = new User("Me", 1234);
        me.setMe(true);

        final Settings settings = mock(Settings.class);
        when(settings.getMe()).thenReturn(me);

        final PropertyFileMessages messages = new PropertyFileMessages("messages.swing");

        mediator = spy(new SwingMediator(componentHandler, mock(ImageLoader.class), settings, messages));

        uiTools = TestUtils.setFieldValueWithMock(mediator, "uiTools", UITools.class);
        controller = TestUtils.setFieldValueWithMock(mediator, "controller", Controller.class);
        TestUtils.setFieldValueWithMock(mediator, "msgController", MessageController.class);
        jmxAgent = TestUtils.setFieldValueWithMock(mediator, "jmxAgent", JMXAgent.class);
        cmdParser = TestUtils.setFieldValueWithMock(mediator, "cmdParser", CommandParser.class);

        when(controller.getUserList()).thenReturn(new SortedUserList());
        when(uiTools.createTitle(anyString())).thenCallRealMethod();
        when(controller.getTopic()).thenReturn(new Topic());
    }

    @Test
    public void constructorShouldThrowExceptionIfComponentHandlerIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Component handler can not be null");

        new SwingMediator(null, mock(ImageLoader.class), mock(Settings.class), mock(Messages.class));
    }

    @Test
    public void constructorShouldThrowExceptionIfImageLoaderIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Image loader can not be null");

        new SwingMediator(mock(ComponentHandler.class), null, mock(Settings.class), mock(Messages.class));
    }

    @Test
    public void constructorShouldThrowExceptionIfSettingsIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Settings can not be null");

        new SwingMediator(mock(ComponentHandler.class), mock(ImageLoader.class), null, mock(Messages.class));
    }

    @Test
    public void constructorShouldThrowExceptionIfMessagesIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Messages can not be null");

        new SwingMediator(mock(ComponentHandler.class), mock(ImageLoader.class), mock(Settings.class), null);
    }

    @Test
    public void constructorShouldValidateComponentHandler() {
        verify(componentHandler).validate();
    }

    @Test
    public void setAwayWhenBackShouldAskAwayMessageAndGoAway() throws CommandException {
        when(uiTools.showInputDialog(anyString(), anyString(), anyString())).thenReturn("Going away");

        mediator.setAway();

        verify(uiTools).showInputDialog("Reason for away?", "Away", null);
        verify(controller).goAway("Going away");
        verify(uiTools, never()).showWarningMessage(anyString(), anyString());
    }

    @Test
    public void setAwayWhenBackShouldUpdateWritingStatusAndClearInputFieldIfCurrentlyWriting() {
        when(uiTools.showInputDialog(anyString(), anyString(), anyString())).thenReturn("Going away");
        when(controller.isWrote()).thenReturn(true);

        mediator.setAway();

        verify(controller).changeWriting(1234, false);
        verify(messageTF).setText("");
    }

    @Test
    public void setAwayWhenBackShouldNotUpdateWritingStatusAndClearInputFieldIfNotCurrentlyWriting() {
        when(uiTools.showInputDialog(anyString(), anyString(), anyString())).thenReturn("Going away");
        when(controller.isWrote()).thenReturn(false);

        mediator.setAway();

        verify(controller, never()).changeWriting(anyInt(), anyBoolean());
        verify(messageTF, never()).setText(anyString());
    }

    @Test
    public void setAwayWhenBackShouldShowWarningMessageIfChangeFails() throws CommandException {
        when(uiTools.showInputDialog(anyString(), anyString(), anyString())).thenReturn("Leaving");
        doThrow(new CommandException("Don't go away")).when(controller).goAway(anyString());

        mediator.setAway();

        verify(controller).goAway("Leaving");
        verify(uiTools).showWarningMessage("Don't go away", "Change away");
    }

    @Test
    public void setAwayWhenBackShouldAskAwayMessageAndDoNothingIfMessageIsNull() {
        when(uiTools.showInputDialog(anyString(), anyString(), anyString())).thenReturn(null);

        mediator.setAway();

        verifyZeroInteractions(controller);
    }

    @Test
    public void setAwayWhenBackShouldAskAwayMessageAndDoNothingIfMessageIsBlank() {
        when(uiTools.showInputDialog(anyString(), anyString(), anyString())).thenReturn(" ");

        mediator.setAway();

        verifyZeroInteractions(controller);
    }

    @Test
    public void setAwayWhenBackShouldRequestFocusOnInputField() {
        mediator.setAway();

        verify(messageTF).requestFocusInWindow();
    }

    @Test
    public void setAwayWhenAwayShouldAskIfBackAndNotChangeIfNo() {
        me.setAway(true);
        me.setAwayMsg("Gone");

        when(uiTools.showOptionDialog(anyString(), anyString())).thenReturn(JOptionPane.NO_OPTION);

        mediator.setAway();

        verify(uiTools).showOptionDialog("Back from 'Gone'?", "Away");
        verifyZeroInteractions(controller);
    }

    @Test
    public void setAwayWhenAwayShouldAskIfBackAndChangeIfYes() throws CommandException {
        me.setAway(true);
        me.setAwayMsg("Gone");

        when(uiTools.showOptionDialog(anyString(), anyString())).thenReturn(JOptionPane.YES_OPTION);

        mediator.setAway();

        verify(uiTools).showOptionDialog("Back from 'Gone'?", "Away");
        verify(controller).comeBack();
        verify(uiTools, never()).showWarningMessage(anyString(), anyString());
    }

    @Test
    public void setAwayWhenAwayShouldShowWarningMessageIfChangeFails() throws CommandException {
        me.setAway(true);
        me.setAwayMsg("Gone");

        when(uiTools.showOptionDialog(anyString(), anyString())).thenReturn(JOptionPane.YES_OPTION);
        doThrow(new CommandException("Don't come back")).when(controller).comeBack();

        mediator.setAway();

        verify(uiTools).showOptionDialog("Back from 'Gone'?", "Away");
        verify(controller).comeBack();
        verify(uiTools).showWarningMessage("Don't come back", "Change away");
    }

    @Test
    public void setAwayWhenAwayShouldRequestFocusOnInputField() {
        me.setAway(true);

        mediator.setAway();

        verify(messageTF).requestFocusInWindow();
    }

    @Test
    public void startShouldLogOnControllerAndActivateJMXAgent() {
        mediator.start();

        verify(controller).start();
        verify(controller).logOn();
        verify(jmxAgent).activate();
    }

    @Test
    public void setTopicShouldUseExistingTopicAsInitialValue() {
        when(controller.getTopic()).thenReturn(new Topic("Initial topic", "Niles", System.currentTimeMillis()));

        mediator.setTopic();

        verify(uiTools).showInputDialog("Change topic?", "Topic", "Initial topic");
    }

    @Test
    public void setTopicShouldNotChangeTopicIfDialogWasCancelled() {
        when(controller.getTopic()).thenReturn(new Topic("Initial topic", "Niles", System.currentTimeMillis()));
        when(uiTools.showInputDialog(anyString(), anyString(), anyString())).thenReturn(null);

        mediator.setTopic();

        verifyZeroInteractions(cmdParser);
        verify(messageTF).requestFocusInWindow();
    }

    @Test
    public void setTopicShouldChangeTopicIfDialogWasAccepted() throws CommandException {
        when(controller.getTopic()).thenReturn(new Topic("Initial topic", "Niles", System.currentTimeMillis()));
        when(uiTools.showInputDialog(anyString(), anyString(), anyString())).thenReturn("new topic");

        mediator.setTopic();

        verify(cmdParser).fixTopic("new topic");
        verify(messageTF).requestFocusInWindow();
        verify(uiTools, never()).showWarningMessage(anyString(), anyString());
    }

    @Test
    public void setTopicShouldShowWarningMessageIfChangingTopicFails() throws CommandException {
        when(controller.getTopic()).thenReturn(new Topic("Initial topic", "Niles", System.currentTimeMillis()));
        when(uiTools.showInputDialog(anyString(), anyString(), anyString())).thenReturn("new topic");
        doThrow(new CommandException("Topic error")).when(cmdParser).fixTopic(anyString());

        mediator.setTopic();

        verify(cmdParser).fixTopic("new topic");
        verify(messageTF).requestFocusInWindow();
        verify(uiTools).showWarningMessage("Topic error", "Change topic");
    }

    @Test
    public void quitShouldExitIfYes() {
        expectedSystemExit.expectSystemExitWithStatus(0);
        expectedSystemExit.checkAssertionAfterwards(new Assertion() {
            @Override
            public void checkAssertion() throws Exception {
                verify(uiTools).showOptionDialog("Are you sure you want to quit?", "Quit");
            }
        });

        when(uiTools.showOptionDialog(anyString(), anyString())).thenReturn(JOptionPane.YES_OPTION);

        mediator.quit();
    }

    @Test
    public void quitShouldNotExitIfCancel() {
        when(uiTools.showOptionDialog(anyString(), anyString())).thenReturn(JOptionPane.CANCEL_OPTION);

        mediator.quit();

        verify(uiTools).showOptionDialog("Are you sure you want to quit?", "Quit");
    }

    @Test
    public void updateTitleAndTrayShouldShowNotConnectedWhenNotConnectedAndNotLoggedOn() {
        when(controller.isConnected()).thenReturn(false);
        when(controller.isLoggedOn()).thenReturn(false);

        mediator.updateTitleAndTray();

        verify(kouChatFrame).setTitle("Me - Not connected - KouChat");
        verify(sysTray).setToolTip("Me - Not connected - KouChat");
    }

    @Test
    public void updateTitleAndTrayShouldShowNotConnectedWhenNotConnectedAndNotLoggedOnEvenWhenAwayAndWithTopicSet() {
        when(controller.isConnected()).thenReturn(false);
        when(controller.isLoggedOn()).thenReturn(false);

        when(controller.getTopic()).thenReturn(new Topic("Topic", "Niles", System.currentTimeMillis()));
        me.setAway(true);

        mediator.updateTitleAndTray();

        verify(kouChatFrame).setTitle("Me - Not connected - KouChat");
        verify(sysTray).setToolTip("Me - Not connected - KouChat");
    }

    @Test
    public void updateTitleAndTrayShouldShowConnectionLostWhenNotConnectedAndLoggedOn() {
        when(controller.isConnected()).thenReturn(false);
        when(controller.isLoggedOn()).thenReturn(true);

        mediator.updateTitleAndTray();

        verify(kouChatFrame).setTitle("Me - Connection lost - KouChat");
        verify(sysTray).setToolTip("Me - Connection lost - KouChat");
    }

    @Test
    public void updateTitleAndTrayShouldShowConnectionLostWhenNotConnectedAndLoggedOnEvenWhenAwayAndWithTopicSet() {
        when(controller.isConnected()).thenReturn(false);
        when(controller.isLoggedOn()).thenReturn(true);

        when(controller.getTopic()).thenReturn(new Topic("Topic", "Niles", System.currentTimeMillis()));
        me.setAway(true);

        mediator.updateTitleAndTray();

        verify(kouChatFrame).setTitle("Me - Connection lost - KouChat");
        verify(sysTray).setToolTip("Me - Connection lost - KouChat");
    }

    @Test
    public void updateTitleAndTrayShouldShowJustNickNameWhenOnline() {
        when(controller.isConnected()).thenReturn(true);
        when(controller.isLoggedOn()).thenReturn(true);

        mediator.updateTitleAndTray();

        verify(kouChatFrame).setTitle("Me - KouChat");
        verify(sysTray).setToolTip("Me - KouChat");
    }

    @Test
    public void updateTitleAndTrayShouldIncludeAwayWhenAway() {
        when(controller.isConnected()).thenReturn(true);
        when(controller.isLoggedOn()).thenReturn(true);
        me.setAway(true);

        mediator.updateTitleAndTray();

        verify(kouChatFrame).setTitle("Me (Away) - KouChat");
        verify(sysTray).setToolTip("Me (Away) - KouChat");
    }

    @Test
    public void updateTitleAndTrayShouldIncludeTopicWhenTopicIsSet() {
        when(controller.isConnected()).thenReturn(true);
        when(controller.isLoggedOn()).thenReturn(true);
        when(controller.getTopic()).thenReturn(new Topic("Christmas time", "Niles", System.currentTimeMillis()));

        mediator.updateTitleAndTray();

        verify(kouChatFrame).setTitle("Me - Topic: Christmas time (Niles) - KouChat");
        verify(sysTray).setToolTip("Me - Topic: Christmas time (Niles) - KouChat");
    }

    @Test
    public void updateTitleAndTrayShouldIncludeBothTopicAndAwayWhenAwayAndTopicIsSet() {
        when(controller.isConnected()).thenReturn(true);
        when(controller.isLoggedOn()).thenReturn(true);
        when(controller.getTopic()).thenReturn(new Topic("Smell you later", "Kenny", System.currentTimeMillis()));
        me.setAway(true);

        mediator.updateTitleAndTray();

        verify(kouChatFrame).setTitle("Me (Away) - Topic: Smell you later (Kenny) - KouChat");
        verify(sysTray).setToolTip("Me (Away) - Topic: Smell you later (Kenny) - KouChat");
    }

    @Test
    public void updateTitleAndTrayShouldUpdateWindowIcon() {
        mediator.updateTitleAndTray();

        verify(kouChatFrame).updateWindowIcon();
    }
}
