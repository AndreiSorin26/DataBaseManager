package database;

import models.Answer;
import models.Question;

import java.util.ArrayList;

public class QuestionDB extends DataBase<Question>
{
    private Integer generalID = 0;
    @Override
    protected Question ConvertToModel(String line)
    {
        String[] values = line.split(",");
        Question question = new Question();
        question.text = values[1];
        question.isSingle = Boolean.parseBoolean(values[2]);
        question.ownerId = Integer.parseInt(values[3]);

        question.answers = new ArrayList<>();
        for( String answerInfo : values[4].split(";") )
        {
            String[] answerValues = answerInfo.split("\\u007c");
            Answer answer = new Answer();
            answer.ID = Integer.parseInt(answerValues[0]);
            answer.text = answerValues[1];
            answer.isCorrect = Boolean.parseBoolean(answerValues[2]);
            question.answers.add(answer);
        }

        return question;
    }

    @Override
    protected String ConvertToString(Question obj)
    {
        StringBuilder answerText = new StringBuilder();
        for(Answer ans :  obj.answers)
            answerText.append(ans.ID).append("|").append(ans.text).append("|").append(ans.isCorrect).append(";");
        answerText.deleteCharAt(answerText.length() - 1);

        return String.join(",", obj.id.toString(), obj.text, obj.isSingle.toString(), obj.ownerId.toString(), answerText.toString());
    }

    @Override
    protected String GetDbHeader() {
        return "id,text,type,ownerId,answers";
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
