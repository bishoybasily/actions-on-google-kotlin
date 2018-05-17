package actions.service.actionssdk

import actions.expected.ConversationTokenSerializer
import actions.expected.Serializer
import actions.expected.deserialize
import actions.framework.Headers
import actions.service.actionssdk.api.*
import actions.service.actionssdk.conversation.*


data class ActionsSdkConversationOptions<
        TConvData, TUserStorage>(override var headers: Headers?,
                                 var body: GoogleActionsV2AppRequest,
                                 override var init: ConversationOptionsInit<TConvData, TUserStorage>? = null,
                                 override var debug: Boolean? = null) : ConversationBaseOptions<TConvData, TUserStorage>



class ActionsSdkConversation<TConvData, TUserStorage>(options: ActionsSdkConversationOptions<TConvData, TUserStorage>) :
        Conversation<TUserStorage>(options = ConversationOptions(request = options.body, headers = options.headers)) {

    var body: GoogleActionsV2AppRequest

    /**
     * Get the current Actions SDK intent.
     *
     * @example
     * ```javascript
     *
     * app.intent('actions.intent.MAIN', conv => {
     *   const intent = conv.intent // will be 'actions.intent.MAIN'
     * })
     * ```
     *
     * @public
     */
    var intent: String

    /**
     * The session data in JSON format.
     * Stored using conversationToken.
     *
     * @example
     * ```javascript
     *
     * app.intent('actions.intent.MAIN', conv => {
     *   conv.data.someProperty = 'someValue'
     * })
     * ```
     *
     * @public
     */
    var data: TConvData

    /** @public */
    init {
        this.body = options.body

        val body = options.body
        val init = options

        val inputs = body.inputs ?: mutableListOf()
        val firstInput = inputs.firstOrNull()

        val intent = firstInput?.intent ?: ""
        val conversation = body.conversation
        val conversationToken = conversation?.conversationToken

        this.intent = intent

        this.data = if (conversationToken != null) {
            deserialize<TConvData>(conversationToken)!!
        } else {
            TODO("Find way to do this in kotlin, or delegate to platform")
//            ((init && init.data) || {})
        }
    }

    fun serialize(): GoogleActionsV2AppResponse {
        if (this._raw != null) {
            TODO("Find way to serialize.  _raw as a string or JsonObject?")
            return this._raw as GoogleActionsV2AppResponse  //TODO REMOVE - ONLY HERE TO COMPILE
        }
        val response = this.response()

        val richResponse = response.richResponse
        val expectUserResponse = response.expectUserResponse
        val userStorage = response.userStorage
        val expectedIntent = response.expectedIntent

        val inputPrompt = GoogleActionsV2InputPrompt(richInitialPrompt = richResponse)

        val possibleIntents = if (expectedIntent != null) {
            mutableListOf<GoogleActionsV2ExpectedIntent>(expectedIntent)
        } else {
            mutableListOf<GoogleActionsV2ExpectedIntent>(GoogleActionsV2ExpectedIntentData(intent = IntentEnum.TEXT.value))
        }
        val expectedInput = GoogleActionsV2ExpectedInput(
                inputPrompt = inputPrompt,
                possibleIntents = possibleIntents)
        val conversationToken = Serializer.stringifyConversationToken(this.data)

        return GoogleActionsV2AppResponse(
                expectUserResponse = expectUserResponse,
                expectedInputs = if (expectUserResponse == true) mutableListOf(expectedInput) else null,
                finalResponse = if (expectUserResponse == true) null else GoogleActionsV2FinalResponse(richResponse = richResponse),
                conversationToken = Serializer.serialize(conversationToken),
                userStorage = userStorage)
    }
}