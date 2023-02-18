package database;

import models.Quiz;

import java.util.ArrayList;

public class QuizDB extends DataBase<Quiz>
{
    private Integer generalID = 0;

    @Override
    protected Quiz ConvertToModel(String line)
    {
        String[] values = line.split(",");
        Quiz quiz = new Quiz();
        quiz.name = values[1];
        quiz.ownerId = Integer.parseInt(values[2]);
        quiz.questionIds = new ArrayList<>();
        for( String questionId : values[3].split(";") )
            quiz.questionIds.add(Integer.parseInt(questionId));
        return quiz;
    }

    @Override
    public String ConvertToString(Quiz obj)
    {
        StringBuilder questionIdsText =  new StringBuilder();
        for( Integer questionId : obj.questionIds )
            questionIdsText.append(questionId.toString()).append(";");
        questionIdsText.deleteCharAt(questionIdsText.length() - 1);

        return String.join(",", obj.id.toString(), obj.name, obj.ownerId.toString(), questionIdsText.toString());
    }

    @Override
    protected String GetDbHeader() {
        return "id,name,ownerId,questionsIds";
    }

    @Override
    protected Integer GetId() {
        return generalID;
    }

    @Override
    protected void SetId(Integer id) {
        generalID = id;
    }
}
