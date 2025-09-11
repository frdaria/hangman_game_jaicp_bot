# require: slotfilling/slotFilling.sc
#   module = sys.zb-common
  
require: patterns.sc
require: func.js 

init:
    bind("postProcess", function($context) {
        //log("/// my log: " + toPrettyString($context));
        $context.session.lastState = $context.currentState;
    });
 
 
 
theme: /

    state: Start
        q!: $regex</start>
        q!: игра
        q!: привет
        a: Я тут слово загадал, чтобы время убить 😎 поиграем?
        buttons:
                "Начать игру"
                
        state: NoStart
            q: Нет
            script:
                $response.replies = $response.replies || [];
                $response.replies.push({
                type: "image",
                imageUrl: "https://i.pinimg.com/736x/9c/60/05/9c60050a93fd612f68812c619f856a6e.jpg",
                });
            a: Ну и ладно...
            a: Если передумаешь, то пиши "игра"
                
                
    state: Hangman
        q!: Начать игру
        a: Напомнить правила?
        buttons:
                "Да"
                "Нет"
 
        state: Rules
            q: Да
            a: Все просто: я загадываю слово, а ты пытаешься его отгадать — по отдельным буквам или целиком. Справишься — победа твоя! 😎 
            a: Но 👉👈 ...  у тебя есть только 6 попыток. Не угадаешь — проиграешь)) окей?
            buttons: 
                "Погнали!"
                
        state: Start
            q: (нет/не надо/не нужно/сам помню/без правил/не сейчас) 
            a: Ок, тогда сразу погнали! 😎
            go!: /Hangman/Play/CreateWord
            
        state: CatchAll
            event: noMatch
            random:
                a: Ты это о чём? Я не совсем понял.
                a: Это что за новый язык? 👀
            go!: {{$session.lastState}}
                

        state: NotStart
            q!: (не хочу/не буду/потом/не сейчас)
            script:
                $response.replies = $response.replies || [];
                $response.replies.push({
                type: "image",
                imageUrl: "https://i.pinimg.com/736x/9c/60/05/9c60050a93fd612f68812c619f856a6e.jpg",
                });
            a: Ну и ладно...
            a: Если передумаешь, то пиши "игра"
                
        
        state: Play
            
            state: CreateWord
                q!: (Погнали! | ещё | еще)
                script:
                    $context.session = {};
                script:
                    var words = ["Котик", "Кофе", "Матча", "Удача"];
                    var original = words[Math.floor(Math.random() * words.length)];
                    $session.originalWord = original;
                    $session.guess = original.toUpperCase();
                    $session.usedLetters = [];
                    
                    # $session.guess = words[Math.floor(Math.random() * words.length)];
                    # $session.guess = $session.guess.charAt(0).toUpperCase() + $session.guess.slice(1);
                    $session.numErrors = 0;
                    # var usedLetters = $session.usedLetters || [];
                    var hiddenWord = "";
                    for (var i = 0; i < $session.guess.length; i++) {
                        hiddenWord += "▁";
                        }
                    $session.hiddenWord = hiddenWord;
                a: Я загадал это слово {{$session.hiddenWord}}
                a: Твой ход!
            
            
            state: GameCheck    
                if: $session.numErrors === 6
                    a: Не-а! Было загадано слово "{{$session.guess}}".
                    script:
                        $response.replies.push({
                        type: "image",
                        imageUrl: "https://i.pinimg.com/736x/ec/c7/8d/ecc78d73ace576e586db9770f87acf19.jpg",
                        });
                    a: Если хочешь поиграть еще, то пиши "ещё" 🤓
                    script:
                        $context.session = {};
                    # go!: /ExitHangman
                    
                elseif: $session.hiddenWord.indexOf("▁") === -1
                    a: 🎉 Урааа, слово отгадано! "{{$session.originalWord}}"! 
                    script:
                        $response.replies.push({
                        type: "image",
                        imageUrl: "https://i.pinimg.com/736x/22/bf/ac/22bfac5521353a98b130f0886a0d2918.jpg",
                        });
                    a: Поиграем ещё немного? Напиши «ещё» 😎
                    script:
                        $context.session = {};  
                    
                else:
                    random:
                        a: Твой ход 😎
                        a: Назови букву
                        a: Идеи? 😎
                        
            
            # state: DefineIntent
            #     # q!: $randomWord
            #     # go!: ../PlayerSayWord
                
            #     q!: $help_word
            #     go!: ../PlayerAskLetter
                
            #     q!: $randomWord
            #     go!: ../PlayerSayWord
                
            #     q!: $help_phrase
            #     go!: ../PlayerAskLetter
            
            
            state: DefineIntent
                q!: $randomWord

                script:
                    $session.randomWord = getUserWord($parseTree)
                    log("////// My LOG USERWORD DEFINE INTENT: " + toPrettyString($parseTree))
                    
                    // нормализуем ввод
                    var randomWord = ($session.randomWord || "").trim();
                    var input = randomWord.toLowerCase();

                    // сохраняем нормализованное слово в сессию
                    $session.randomWord = randomWord;
        # // --- ключевые подстроки для распознавания запроса помощи ---
                    var helpKeywords = [
                        "подсказ",
                        "подскажи",
                        "помощь",
                        "help",
                        "хелп",
                        "дай букв",
                        "букв",
                        "не знаю",
                        "не могу",
                        "помоги"
                    ];

                    // проверка: если слово совпадает или содержит одно из ключевых слов
                    $session.isHelp = false;
                    for (var i = 0; i < helpKeywords.length; i++) {
                        if (input.indexOf(helpKeywords[i]) !== -1) {
                            $session.isHelp = true;
                            break;
                        }
                    }

                if: $session.isHelp
                    go!: ../PlayerAskLetter
                else:
                    go!: ../PlayerSayWord
                    
                # q!: $help_phrase
                # go!: ../PlayerAskLetter




            state: CatchAll
                event: noMatch
                random:
                    a: Что? 👀 Назови букву или слово целиком
                    a: Ничего не понял...Назови букву или слово целиком
                    go!: {{$session.lastState}}
                    
    
    
    
            state: PlayerSayLetter
                q!: * $ruLetter *

                script:
                        if (!$session.usedLetters) {
                            $session.usedLetters = [];
                        } else {
                            if (!Array.isArray($session.usedLetters)) {
                                $session.usedLetters = [];
                            }
                        }

                        $session.hiddenWordArray = $session.hiddenWord.split('');
                        
                        $session.currentLetter = getUserLetter($parseTree)
                    # $session.currentLetter = toPrettyString(currentLetter)
                    # $session.currentLetter = getUserLetter($parseTree)
                        
                        log("////// My LOG USERLETTER: " + toPrettyString($parseTree))

                if: $session.usedLetters.indexOf($session.currentLetter) !== -1
                        a: Ты уже называл эту букву! Попробуй другую.
                        # a: Текущая буква - {{$session.currentLetter}}
                        a: Использованные буквы: {{$session.usedLetters.join(", ")}}
                        # a: {{$session.hiddenWord}}
                        a: У тебя осталось {{6 - $session.numErrors}} попыток
                        # go!: /Hangman/Play
                        go!: ../GameCheck
                        
                elseif: $session.guess.indexOf($session.currentLetter) === -1
                        random:
                            a: Такой буквы там нет :)
                            a: Не угадал :)
                            a: Ха-ха! Такой буквы нет!
                        
                        script:
                            $session.numErrors = $session.numErrors + 1;
                            # $session.usedLetters.push($session.currentLetter);
                            if (Array.isArray($session.usedLetters)) {
                                $session.usedLetters.push($session.currentLetter);
                            } else {
                                $session.usedLetters = [$session.currentLetter];
                                }
                        a: {{$session.hiddenWord}}
                        a: Использованные буквы: {{$session.usedLetters.join(", ")}}
                        # a: Текущая буква - {{$session.currentLetter}}
                        a: У тебя осталось {{6 - $session.numErrors}} попыток
                        # go!: /Hangman/Play
                        go!: ../GameCheck

        
                else:
                    random:
                        a: Да, такая буква есть
                        a: Да, там есть такая буква :)
                        a: Неплохо!
                        a: Окак.... наверное, моё слово слишком простое?
                        
                    script:
                            if (Array.isArray($session.usedLetters)) {
                                $session.usedLetters.push($session.currentLetter);
                            } else {
                                $session.usedLetters = [$session.currentLetter];
                            }

                            var newHidden = "";
                            for (var i = 0; i < $session.guess.length; i++) {
                                var originalChar = $session.guess.charAt(i);
                                var hiddenChar = $session.hiddenWord.charAt(i);

                                if (originalChar.toUpperCase() === $session.currentLetter) {
                                    newHidden += originalChar;   // раскрываем угаданную букву
                                } else {
                                    newHidden += hiddenChar;     // оставляем как было
                                }
                            }
                            $session.hiddenWord = newHidden;
                    a: {{$session.hiddenWord}}
                    a: Использованные буквы: {{$session.usedLetters.join(", ")}}
                    # go!: /Hangman/Play
                    go!: ../GameCheck

   
            state: PlayerSayWord
             
                # q!: $randomWord
                
                script:
                        
                    $session.userWord = getUserWord($parseTree)
                    script:
                            log("////// My LOG USERWORD: " + toPrettyString($parseTree))
                        
                    var userWord =  $session.userWord;
                    # $reactions.answer(userWord);
                        
                    var guessWord = $session.guess;

                    if (userWord === $session.guess) {
                        $session.hiddenWord = $session.originalWord;
                        $session.isWin = true;
                    } else {
                        $session.numErrors = $session.numErrors + 1;
                        $session.isWin = false;
                    }

                if: $session.isWin 
                    a: 🎉 Ура! Ты угадал слово "{{$session.originalWord}}"!
                    go!: ../GameCheck

                else:
                    a: Нет, это не "{{$session.userWord}}" 
                    a: Попробуй ещё! Текущее слово: {{$session.hiddenWord}}
                    a: У тебя осталось {{6 - $session.numErrors}} попыток
                    # go!: /Hangman/Play  
                    go!: ../GameCheck
        
        
            state: PlayerAskLetter
                q!: $help_phrase
                script:
                    if (!$session.usedLetters) {
                        $session.usedLetters = [];
                    } else {
                        if (!Array.isArray($session.usedLetters)) {
                            $session.usedLetters = [];
                        }
                    }

                    var hiddenArray = $session.hiddenWord.split('');
                    var guessWord = $session.guess;
                    $session.letterToReveal = null;

                    for (var i = 0; i < hiddenArray.length; i++) {
                        if (hiddenArray[i] === "▁") {
                            $session.letterToReveal = guessWord[i];
                            hiddenArray[i] = $session.letterToReveal;
                            break;
                        }
                    }

                    $session.hiddenWord = hiddenArray.join('');

                    if ($session.letterToReveal) {
                        if (Array.isArray($session.usedLetters)) {
                            $session.usedLetters.push($session.letterToReveal);
                        } else {
                            $session.usedLetters = [$session.letterToReveal];
                        }
                    }

                a: Открываю букву "{{$session.letterToReveal}}"
                a: {{$session.hiddenWord}}
                a: У тебя осталось {{6 - $session.numErrors}} попыток
                go!: ../GameCheck
        
    

            state: PlayerAskLettersUsed
                q!: $lettersUsed
                script:
                    if (!$session.usedLetters) {
                        $session.usedLetters = [];
                    } else {
                        if (!Array.isArray($session.usedLetters)) {
                        $session.usedLetters = [];
                        }
                    }

                    var lettersArray = $session.usedLetters;
                    if (lettersArray.length === 0) {
                        $session.lettersMessage = "Ты еще не называл ни одной буквы!";
                    } else {
                        $session.lettersMessage = "Уже были эти буквы: " + lettersArray.join(", ");
                    }

                a: {{$session.lettersMessage}}
                a: Текущее слово: {{$session.hiddenWord}}
                a: Осталось попыток: {{6 - $session.numErrors}}
                go!: ../GameCheck
                

    
    state: NoMatch
        event!: noMatch
        random:
             a: Прости, не понял
             a: Что ты имеешь в виду?
             a: Повтори еще раз
        go!: {{$session.lastState}}

    

    # state: Hello
    #     intent!: /привет
    #     a: Привет привет

 



    # state: Match
    #     event!: match
    #     a: {{$context.intent.answer}}