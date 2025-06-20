package com.nhatpham.dishcover.presentation.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.chatbot.ChatMessage
import com.nhatpham.dishcover.domain.model.chatbot.MessageSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ChatbotState())
    val state: StateFlow<ChatbotState> = _state.asStateFlow()

    fun onEvent(event: ChatbotEvent) {
        when (event) {
            is ChatbotEvent.MessageChanged -> {
                _state.update { it.copy(currentMessage = event.message) }
            }

            is ChatbotEvent.SendMessage -> {
                sendMessage()
            }
        }
    }

    private fun sendMessage() {
        val currentMessage = _state.value.currentMessage.trim()
        if (currentMessage.isEmpty()) return

        val userMessage = ChatMessage(
            content = currentMessage,
            sender = MessageSender.USER
        )

        _state.update {
            it.copy(
                messages = it.messages + userMessage,
                currentMessage = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            val responseDelay = (800..2500).random()
            delay(responseDelay.toLong())

            val botResponse = generateResponse(currentMessage)
            val botMessage = ChatMessage(
                content = botResponse,
                sender = MessageSender.BOT
            )

            _state.update {
                it.copy(
                    messages = it.messages + botMessage,
                    isLoading = false
                )
            }
        }
    }

    private fun generateResponse(userMessage: String): String {
        val lowerMessage = userMessage.lowercase()

        return when {
            lowerMessage.contains("hello") || lowerMessage.contains("hi") ||
                    lowerMessage.contains("hey") || lowerMessage.contains("good morning") ||
                    lowerMessage.contains("good afternoon") || lowerMessage.contains("good evening") -> {
                val greetings = listOf(
                    "Hello there! Welcome to your personal cooking assistant! 👨‍🍳 I'm here to help with recipes, techniques, and all things culinary. What's cooking today?",
                    "Hi! Great to see you in the kitchen! Whether you're a beginner or a seasoned chef, I'm here to help. What would you like to cook?",
                    "Hey! Ready to create something delicious? I can help with recipes, cooking tips, ingredient substitutions, and more. What's on your mind?",
                    "Good day! I'm your cooking companion, ready to help turn your kitchen adventures into tasty successes! What can I assist you with?",
                    "Hello! Whether you're looking for quick weeknight dinners or weekend cooking projects, I'm here to guide you. What sounds good to you?"
                )
                greetings.random()
            }

            lowerMessage.contains("rice") -> {
                when {
                    lowerMessage.contains("fried rice") -> {
                        "Perfect fried rice recipe: Start with day-old rice (it's drier and won't get mushy). Heat oil in a wok or large pan, scramble eggs first and set aside. Add rice, breaking up clumps, then soy sauce, vegetables, and eggs back in. The key is high heat and quick movement!"
                    }
                    lowerMessage.contains("risotto") -> {
                        "Risotto is all about technique! Use Arborio rice, keep your broth warm, add one ladle at a time, stirring constantly. It takes about 20 minutes of patience. The rice should be creamy but still have a slight bite (al dente). Would you like a specific flavor variation?"
                    }
                    lowerMessage.contains("spanish") || lowerMessage.contains("paella") -> {
                        "Spanish rice (or paella) uses bomba rice or Arborio as substitute. Toast the rice briefly in oil, add saffron-infused broth, don't stir once you add liquid! Cook 18-20 minutes, then rest 5 minutes. The bottom should get slightly crispy (socarrat)."
                    }
                    lowerMessage.contains("sushi") -> {
                        "Sushi rice needs short-grain rice, rice vinegar, sugar, and salt. Cook rice with slightly less water, then fold in seasoned vinegar while rice is warm. Fan it while mixing to cool quickly and get that perfect sticky texture!"
                    }
                    lowerMessage.contains("coconut") -> {
                        "Coconut rice is divine! Replace half the water with coconut milk, add a pinch of salt and sugar. Cook normally, but watch carefully as coconut milk can burn. Great with Thai or Caribbean dishes!"
                    }
                    lowerMessage.contains("sticky") && !lowerMessage.contains("mushy") -> {
                        "For sticky rice: Use glutinous rice, soak 4+ hours, steam (don't boil!) for 25-30 minutes. Perfect for Asian desserts or savory dishes. It should be translucent and very sticky when done."
                    }
                    lowerMessage.contains("mushy") || lowerMessage.contains("soggy") -> {
                        "Mushy rice is fixable! Next time: rinse rice until water runs clear, use less water (1:1.5 ratio for firmer rice), don't lift the lid while cooking, and let it rest off heat for 5 minutes. What type of rice are you using?"
                    }
                    lowerMessage.contains("pilaf") -> {
                        "Rice pilaf starts by toasting the rice in oil until lightly golden, then adding hot broth (2:1 ratio). Bake covered at 350°F for 18 minutes. This method gives individual, fluffy grains every time!"
                    }
                    lowerMessage.contains("brown rice") -> {
                        "Brown rice needs more water and time! Use 1:2.5 ratio, bring to boil, then simmer 45 minutes. It's nuttier and chewier than white rice. Soak it for 30 minutes first to reduce cooking time."
                    }
                    lowerMessage.contains("jasmine") -> {
                        "Jasmine rice is fragrant and slightly sticky. Rinse well, use 1:1.5 ratio, bring to boil then simmer 15 minutes. Perfect with Thai and Southeast Asian dishes!"
                    }
                    lowerMessage.contains("basmati") -> {
                        "Basmati rice is long-grain and fluffy. Soak 30 minutes, rinse well, use 1:1.5 ratio. Add whole spices like cardamom or bay leaves for extra flavor. Great with Indian and Middle Eastern dishes!"
                    }
                    lowerMessage.contains("why rinse") || lowerMessage.contains("wash rice") -> {
                        "Rinsing rice removes excess starch that makes it gummy! Rinse in cold water until the water runs clear - usually 3-4 rinses. This gives you separate, fluffy grains instead of sticky clumps."
                    }
                    lowerMessage.contains("ratio") || lowerMessage.contains("water") -> {
                        "Rice-to-water ratios: White rice 1:1.5-2, Brown rice 1:2.5, Jasmine 1:1.5, Basmati 1:1.5, Arborio 1:3-4 (for risotto). Adjust based on your preferred texture - less water for firmer rice!"
                    }
                    lowerMessage.contains("flavor") || lowerMessage.contains("plain") || lowerMessage.contains("boring") -> {
                        "Jazz up rice easily! Cook in chicken/vegetable broth instead of water, add bay leaves, garlic, onion powder, or turmeric while cooking. Finish with butter, herbs, toasted nuts, or coconut flakes!"
                    }
                    lowerMessage.contains("leftover") -> {
                        "Leftover rice is perfect for fried rice! Store in fridge up to 4 days. Reheat with a splash of water in microwave, or use directly for fried rice. Day-old rice actually works better than fresh for frying!"
                    }
                    lowerMessage.contains("minute rice") || lowerMessage.contains("instant") -> {
                        "Instant rice is pre-cooked and dehydrated, so it cooks in 5 minutes. Just follow package directions - usually equal parts rice and boiling water, cover, wait 5 minutes. Not as fluffy as regular rice but super convenient!"
                    }
                    lowerMessage.contains("why rest") || lowerMessage.contains("why wait") -> {
                        "Resting rice for 5-10 minutes after cooking lets the moisture redistribute evenly and firms up the grains. It prevents mushiness when you fluff it! Patience makes perfect rice."
                    }
                    lowerMessage.contains("burnt") || lowerMessage.contains("stuck") -> {
                        "If rice burns/sticks: Don't stir it! Turn off heat, add 2 tablespoons water, cover tightly, let sit 10 minutes. The steam might save the top layer. Use a non-stick pot and proper heat control next time."
                    }
                    lowerMessage.contains("how much") || lowerMessage.contains("serving") -> {
                        "Rice servings: 1/4 cup dry rice per person as a side, 1/2 cup per person as a main. 1 cup dry rice feeds 4 people as a side dish, 2 people as a main. Rice roughly triples in volume when cooked!"
                    }
                    lowerMessage.contains("rice cooker") -> {
                        "Rice cookers are foolproof! Rinse rice, add to cooker with appropriate water ratio, press button. It automatically switches to 'warm' when done. Let it rest 10 minutes before opening. Perfect rice every time!"
                    }
                    lowerMessage.contains("different types") || lowerMessage.contains("varieties") -> {
                        "Rice varieties: Long-grain (fluffy, separate) like Basmati, Medium-grain (slightly sticky) like Jasmine, Short-grain (very sticky) like sushi rice. Each has different cooking methods and best uses!"
                    }
                    else -> {
                        "Perfect rice fundamentals: Rinse until water runs clear, use proper ratios (1:1.5 for most white rice), bring to boil then simmer covered 18 minutes, rest 5 minutes, then fluff with a fork. What specific rice dish interests you?"
                    }
                }
            }

            lowerMessage.contains("thank") -> {
                val thanks = listOf(
                    "You're so welcome! Happy cooking! 🍳 Hope your rice turns out perfectly fluffy!",
                    "My pleasure! Rice can be tricky but you've got this. Let me know how it turns out!",
                    "Glad I could help! Perfect rice takes practice - keep experimenting!",
                    "You're welcome! May your rice always be fluffy and delicious!",
                    "Happy to help! Rice is such a staple - master it and you'll eat well forever!"
                )
                thanks.random()
            }

            lowerMessage.contains("help") || lowerMessage.contains("problem") ||
                    lowerMessage.contains("wrong") || lowerMessage.contains("fix") -> {
                "Rice troubles happen to everyone! Tell me what's going wrong - is it mushy, burnt, too dry, or something else? I'll help troubleshoot your specific rice situation!"
            }

            lowerMessage.contains("but what if") || lowerMessage.contains("what about") ||
                    lowerMessage.contains("however") || lowerMessage.contains("instead") -> {
                "Great rice follow-up question! Rice cooking has many variations depending on the type and dish. Tell me your specific situation and I'll suggest the best approach!"
            }

            lowerMessage.contains("cook") || lowerMessage.contains("cooking") -> {
                "Rice is such a fundamental cooking skill! It's the base for so many cuisines worldwide. Whether you want simple steamed rice, exotic pilaf, creamy risotto, or perfect fried rice - I can guide you through it!"
            }

            else -> {
                val defaultResponses = listOf(
                    "I'm here to help with rice dishes! Are you looking to make fried rice, risotto, pilaf, or just perfect steamed rice?",
                    "Rice is wonderfully versatile! What type of rice dish are you planning? I can help with techniques, ratios, and flavoring!",
                    "Let's talk rice! Whether it's troubleshooting sticky rice or learning a new rice recipe, I'm here to help. What's your rice question?",
                    "Rice cooking got you puzzled? I can help with everything from basic steamed rice to complex dishes like paella and risotto!",
                    "Rice is the foundation of so many great meals! What rice technique or recipe would you like to master today?"
                )
                defaultResponses.random()
            }
        }
    }
}

data class ChatbotState(
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false
)

sealed class ChatbotEvent {
    data class MessageChanged(val message: String) : ChatbotEvent()
    object SendMessage : ChatbotEvent()
}