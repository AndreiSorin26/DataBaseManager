package com.example.project;

import database.QuestionDB;
import database.QuizDB;
import database.SubmissionDB;
import database.UserDB;
import models.*;
import utils.ArgPaser.ArgParser;
import utils.ArgPaser.Args;
import utils.JSON.JSON;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Tema1
{
    public static UserDB users;
    public static QuestionDB questions;
    public static QuizDB quizzes;
    public static SubmissionDB submissions;

	public static void main(final String[] args)
    {
        if( args == null )
        {
            System.out.println("Hello world!");
            return;
        }

        users = new UserDB();
        questions = new QuestionDB();
        quizzes = new QuizDB();
        submissions = new SubmissionDB();

        Args newArgs = ArgParser.Parse(args);
        JSON response = null;
        if( newArgs.command.equals("cleanup-all" ))
        {
            response = CleanUpAll();
            System.out.println(response);
            return;
        }

        Answer.generalID = 1;
        users.Load("src/main/java/database/users.csv");
        questions.Load("src/main/java/database/questions.csv");
        quizzes.Load("src/main/java/database/quizzes.csv");
        submissions.Load("src/main/java/database/submissions.csv");

        if( newArgs.command.equals("create-user") )
            response = CreateUser(newArgs.arguments);
        if( newArgs.command.equals("create-question") )
            response = CreateQuestion(newArgs.arguments);
        if( newArgs.command.equals("get-question-id-by-text") )
            response = GetQuestionById(newArgs.arguments);
        if( newArgs.command.equals("get-all-questions") )
            response = GetAllQuestions(newArgs.arguments);
        if( newArgs.command.equals("create-quizz") )
            response = CreteQuiz(newArgs.arguments);
        if( newArgs.command.equals("get-quizz-by-name") )
            response = GetQuizByName(newArgs.arguments);
        if( newArgs.command.equals("get-all-quizzes") )
            response = GetAllQuizzes(newArgs.arguments);
        if( newArgs.command.equals("get-quizz-details-by-id") )
            response = GetQuizDetails(newArgs.arguments);
        if( newArgs.command.equals("delete-quizz-by-id") )
            response = DeleteQuiz(newArgs.arguments);
        if( newArgs.command.equals("submit-quizz") )
            response = SubmitQuiz(newArgs.arguments);
        if( newArgs.command.equals("get-my-solutions") )
            response = GetMySolutions(newArgs.arguments);

        System.out.println(response);

        users.WriteBack(new File("src/main/java/database/users.csv"));
        questions.WriteBack(new File("src/main/java/database/questions.csv"));
        quizzes.WriteBack(new File("src/main/java/database/quizzes.csv"));
        submissions.WriteBack(new File("src/main/java/database/submissions.csv"));
    }

    public static JSON CreateUser(Map<String, String> args)
    {
        if( !args.containsKey("u") )
            return JSON.Error("Please provide username");
        if( !args.containsKey("p") )
            return JSON.Error("Please provide password");

        if( users.Exists(user -> user.username.equals(args.get("u"))) )
            return JSON.Error("User already exists");

        User user = new User();
        user.username = args.get("u");
        user.password = args.get("p");
        users.Put(user);

        return JSON.Ok("User created successfully");
    }

    public static JSON CreateQuestion(Map<String, String> args)
    {
        if( !args.containsKey("u") || !args.containsKey("p") )
            return JSON.Error("You need to be authenticated");
        User user = users.GetOneIf(u -> u.username.equals(args.get("u")) && u.password.equals(args.get("p")));
        if( user == null )
            return JSON.Error("Login failed");

        if( !args.containsKey("text") )
            return JSON.Error("No question text provided");
        if( questions.Exists(q -> q.text.equals(args.get("text"))) )
            return JSON.Error("Question already exists");

        var answerTextIndexes = args.keySet().stream().filter(arg -> arg.matches("answer-[1-9][0-9]*$")).map(arg -> Integer.parseInt(arg.split("-")[1])).collect(Collectors.toList());
        var answerValIndexes = args.keySet().stream().filter(arg -> arg.contains("is-correct")).map( arg -> Integer.parseInt(arg.split("-")[1]) ).collect(Collectors.toList());
        for( var index : answerTextIndexes )
            if( !answerValIndexes.contains(index) )
                return JSON.Error("Answer " + index + " has no answer correct flag");
        for( var index : answerValIndexes )
            if( !answerTextIndexes.contains(index) )
                return JSON.Error("Answer " + index + " has no answer description");

        if( args.keySet().stream().noneMatch(arg -> arg.matches("answer-[1-9][0-9]*$") ) )
            return JSON.Error("No answer provided");
        if( args.keySet().stream().filter( arg -> arg.matches("answer-[1-9][0-9]*$") ).count() == 1 )
            return JSON.Error("Only one answer provided");
        if( args.keySet().stream().filter( arg -> arg.matches("answer-[1-9][0-9]*$") ).count() > 5 )
            return JSON.Error("More than 5 answers were submitted");

        var rightAnswerCount = args.keySet().stream().filter(arg -> arg.contains("is-correct")).map(args::get).filter(ans -> ans.equals("1")).count();
        if ( args.get("type").equals("single") && rightAnswerCount > 1 )
            return JSON.Error("Single correct answer question has more than one correct answer");

        var answersCount = args.keySet().stream().filter(arg -> arg.matches("answer-[1-9][0-9]*$")).map(args::get).count();
        var uniqueAnswersCount = args.keySet().stream().filter(arg -> arg.matches("answer-[1-9][0-9]*$")).map(args::get).distinct().count();
        if( answersCount != uniqueAnswersCount )
            return JSON.Error("Same answer provided more than once");

        Question question = new Question();
        question.ownerId = user.id;
        question.text = args.get("text");
        question.isSingle = args.get("type").equals("single");
        question.answers = new ArrayList<>();

        var answerTexts = args.keySet().stream().filter(arg -> arg.matches("answer-[1-9][0-9]*$")).sorted().collect(Collectors.toList());
        for (String answerText : answerTexts)
        {
            Answer answer = new Answer();
            answer.text = args.get(answerText);
            String answerVal = args.get("answer-" + answerText.split("-")[1] + "-is-correct");
            answer.isCorrect = answerVal.equals("1");
            question.answers.add(answer);
        }
        questions.Put(question);

        return JSON.Ok("Question added successfully");
    }

    public static JSON GetQuestionById(Map<String, String> args)
    {
        if( !args.containsKey("u") || !args.containsKey("p") )
            return JSON.Error("You need to be authenticated");
        User user = users.GetOneIf(u -> u.username.equals(args.get("u")) && u.password.equals(args.get("p")));
        if( user == null )
            return JSON.Error("Login failed");

        Question question = questions.GetOneIf( q -> q.text.equals(args.get("text")) );
        if( question == null )
            return JSON.Error("Question does not exist");

        return JSON.Ok(question.id.toString());
    }

    public static JSON GetAllQuestions(Map<String, String> args)
    {
        if( !args.containsKey("u") || !args.containsKey("p") )
            return JSON.Error("You need to be authenticated");
        User user = users.GetOneIf(u -> u.username.equals(args.get("u")) && u.password.equals(args.get("p")));
        if( user == null )
            return JSON.Error("Login failed");

        List<JSON> questionsJson = new ArrayList<>();
        for( Question question : questions.GetIf(q -> true) )
        {
            JSON questionJson = new JSON(false);
            questionJson.put("question_id", question.id.toString());
            questionJson.put("question_name", question.text);
            questionsJson.add(questionJson);
        }

        return JSON.Ok(questionsJson.toString());
    }

    public static JSON CreteQuiz(Map<String, String> args)
    {
        if( !args.containsKey("u") || !args.containsKey("p") )
            return JSON.Error("You need to be authenticated");
        User user = users.GetOneIf(u -> u.username.equals(args.get("u")) && u.password.equals(args.get("p")));
        if( user == null )
            return JSON.Error("Login failed");

        if( quizzes.Exists( q -> q.name.equals(args.get("name")) ) )
            return JSON.Error("Quizz name already exists");

        List<Integer> questionIds = args.keySet().stream().filter(arg -> arg.contains("question")).map(s -> Integer.parseInt(args.get(s))).collect(Collectors.toList());
        if( questionIds.size() > 10 )
            return JSON.Error("Quizz has more than 10 questions");

        for( Integer id : questionIds )
            if( !questions.Exists( q -> q.id.equals(id)) )
            {
                Optional<String> maybeId = args.keySet().stream().filter(s -> s.contains("question") && id == Integer.parseInt(args.get(s))).findFirst();
                String qId = maybeId.orElse("").split("-")[1];
                return JSON.Error("Question ID for question " + qId + " does not exist");
            }

        Quiz quiz = new Quiz();
        quiz.ownerId = user.id;
        quiz.name = args.get("name");
        quiz.questionIds = questionIds;
        quizzes.Put(quiz);
        return JSON.Ok("Quizz added succesfully");
    }

    public static JSON GetQuizByName(Map<String, String> args)
    {
        if( !args.containsKey("u") || !args.containsKey("p") )
            return JSON.Error("You need to be authenticated");
        User user = users.GetOneIf(u -> u.username.equals(args.get("u")) && u.password.equals(args.get("p")));
        if( user == null )
            return JSON.Error("Login failed");

        Quiz quiz = quizzes.GetOneIf(q -> q.name.equals(args.get("name")));
        if( quiz == null )
            return JSON.Error("Quizz does not exist");

        return JSON.Ok(quiz.id.toString());
    }

    public static JSON GetAllQuizzes(Map<String, String> args)
    {
        if( !args.containsKey("u") || !args.containsKey("p") )
            return JSON.Error("You need to be authenticated");
        User user = users.GetOneIf(u -> u.username.equals(args.get("u")) && u.password.equals(args.get("p")));
        if( user == null )
            return JSON.Error("Login failed");

        List<JSON> quizzesJson = new ArrayList<>();
        for( Quiz quiz : quizzes.GetIf(q -> true) )
        {
            JSON quizJson = new JSON(false, new String[] {"quizz_id", "quizz_name", "is_completed"}, true);
            quizJson.put("quizz_id", quiz.id.toString());
            quizJson.put("quizz_name", quiz.name);
            quizJson.put("is_completed", submissions.Exists(s -> s.quizId.equals(quiz.id) && s.submitterId.equals(user.id)) ? "True" : "False");
            quizzesJson.add(quizJson);
        }

        return JSON.Ok(quizzesJson.toString());
    }

    public static JSON GetQuizDetails(Map<String, String> args)
    {
        if( !args.containsKey("u") || !args.containsKey("p") )
            return JSON.Error("You need to be authenticated");
        User user = users.GetOneIf(u -> u.username.equals(args.get("u")) && u.password.equals(args.get("p")));
        if( user == null )
            return JSON.Error("Login failed");

        Quiz quiz = quizzes.GetById(Integer.parseInt(args.get("id")));
        if( quiz == null )
            return JSON.Error("Quizz with id " + args.get("id") + " does not exists");

        List<JSON> questionsJSON = new ArrayList<>();
        for( Question question : questions.GetIf(q -> true) )
        {
            JSON questionJSON = new JSON(false, new String[]{"question-name", "question_index", "question_type"}, false);
            questionJSON.put("question-name", question.text);
            questionJSON.put("question_index", question.id);
            questionJSON.put("question_type", question.isSingle ? "single" : "multiple");

            List<JSON> answersJSON = new ArrayList<>();
            for (Answer answer : question.answers) {
                JSON answerJSON = new JSON(false, new String[]{"answer_name", "answer_id"}, false);
                answerJSON.put("answer_name", answer.text);
                answerJSON.put("answer_id", answer.ID);
                answersJSON.add(answerJSON);
            }

            questionJSON.put("answers", answersJSON.toString());
            questionsJSON.add(questionJSON);
        }

        return JSON.Ok(questionsJSON.toString());
    }

    public static JSON SubmitQuiz(Map<String, String> args)
    {
        if( !args.containsKey("u") || !args.containsKey("p") )
            return JSON.Error("You need to be authenticated");
        User user = users.GetOneIf(u -> u.username.equals(args.get("u")) && u.password.equals(args.get("p")));
        if( user == null )
            return JSON.Error("Login failed");

        if( !args.containsKey("quiz-id") )
            return JSON.Error("No quizz identifier was provided");

        Integer quizId = Integer.parseInt(args.get("quiz-id"));
        if( submissions.Exists(s -> s.submitterId.equals(user.id) && s.quizId.equals(quizId)) )
            return JSON.Error("You already submitted this quizz");

        Quiz quiz = quizzes.GetById(Integer.parseInt(args.get("quiz-id")));
        if( quiz == null )
            return JSON.Error("No quiz was found");
        if(quiz.ownerId.equals(user.id))
            return JSON.Error("You cannot answer your own quizz");

        List<Question> quizQuestions = quiz.questionIds.stream().map(questions::GetById).collect(Collectors.toList());
        List<String> answerTags = args.keySet().stream().filter(arg -> arg.contains("answer")).collect(Collectors.toList());
        for( String answerTag : answerTags )
        {
            boolean exists = false;
            Integer id = Integer.parseInt(answerTag.split("-")[2]);
            for( Question question : quizQuestions )
                for( Answer ans : question.answers )
                    if (ans.ID.equals(id))
                    {
                        exists = true;
                        break;
                    }

            if(!exists)
                return JSON.Error("Answer ID for answer " + answerTag.split("-")[2] + " does not exist");
        }

        List<Integer> answerIds = args.keySet().stream().filter(arg -> arg.contains("answer")).map(args::get).map(Integer::parseInt).distinct().collect(Collectors.toList());
        double points = 0;
        for( Question question :  quizQuestions )
        {
            double okPoints = 1.0 / question.answers.stream().filter(ans -> ans.isCorrect).count();
            double notOkPoints = -1.0 / question.answers.stream().filter(ans -> !ans.isCorrect).count();
            double questionPoints = 0;
            for( Answer ans : question.answers )
                if( answerIds.contains(ans.ID) )
                    questionPoints += ans.isCorrect ? okPoints : notOkPoints;

            points += questionPoints/quizQuestions.size();
        }

        Submission submission = new Submission();
        submission.submitterId = user.id;
        submission.quizId = quiz.id;
        submission.answers = answerIds;
        submission.score = Math.max(0, (int)Math.round(points*100));
        submissions.Put(submission);

        return JSON.Ok(submission.score + " points");
    }

    public static JSON DeleteQuiz(Map<String, String> args)
    {
        if( !args.containsKey("u") || !args.containsKey("p") )
            return JSON.Error("You need to be authenticated");
        User user = users.GetOneIf(u -> u.username.equals(args.get("u")) && u.password.equals(args.get("p")));
        if( user == null )
            return JSON.Error("Login failed");

        if( !args.containsKey("id") )
            return JSON.Error("No quizz identifier was provided");
        Quiz quiz = quizzes.GetById(Integer.parseInt(args.get("id")));
        if( quiz == null )
            return JSON.Error("No quiz was found");

        quizzes.Delete(quiz);
        return JSON.Ok("Quizz deleted successfully");
    }

    public static JSON GetMySolutions(Map<String, String> args)
    {
        if( !args.containsKey("u") || !args.containsKey("p") )
            return JSON.Error("You need to be authenticated");
        User user = users.GetOneIf(u -> u.username.equals(args.get("u")) && u.password.equals(args.get("p")));
        if( user == null )
            return JSON.Error("Login failed");

        List<JSON> quizzesDetails = new ArrayList<>();
        List<Submission> userSubmissions = submissions.GetIf(s -> s.submitterId.equals(user.id));
        for( Submission submission : userSubmissions )
        {
            Quiz quiz = quizzes.GetById(submission.quizId);
            JSON quizDetails = new JSON(false, new String[] {"quiz-id", "quiz-name", "score", "index_in_list"}, true);
            quizDetails.put("quiz-id", quiz.id.toString());
            quizDetails.put("quiz-name", quiz.name);
            quizDetails.put("score", submission.score);
            quizDetails.put("index_in_list", userSubmissions.indexOf(submission) + 1);
            quizzesDetails.add(quizDetails);
        }

        return JSON.Ok(quizzesDetails.toString());
    }

    public static JSON CleanUpAll()
    {
        users.Clean("src/main/java/database/users.csv");
        questions.Clean("src/main/java/database/questions.csv");
        quizzes.Clean("src/main/java/database/quizzes.csv");
        submissions.Clean("src/main/java/database/submissions.csv");
        return JSON.Ok("Cleanup finished successfully");
    }
}
