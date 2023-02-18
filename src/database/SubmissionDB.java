package database;

import models.Submission;

import java.util.ArrayList;

public class SubmissionDB extends DataBase<Submission>
{
    private Integer generalID = 0;

    @Override
    protected Submission ConvertToModel(String line) {
        String[] values = line.split(",");

        Submission submission = new Submission();
        submission.id = Integer.parseInt(values[0]);
        submission.submitterId = Integer.parseInt(values[1]);
        submission.quizId = Integer.parseInt(values[2]);
        submission.answers = new ArrayList<>();

        for( String answer : values[3].split(";") )
            submission.answers.add(Integer.parseInt(answer));
        submission.score = Integer.parseInt(values[4]);

        return  submission;
    }

    @Override
    protected String ConvertToString(Submission obj) {
        StringBuilder answers = new StringBuilder();
        for( Integer answer : obj.answers )
            answers.append(answer).append(";");
        answers.deleteCharAt(answers.length() - 1);

        return String.join(",", obj.id.toString(), obj.submitterId.toString(), obj.quizId.toString(), answers.toString(), obj.score.toString());
    }

    @Override
    protected String GetDbHeader() {
        return "id,submitterId,quizId,answers,score";
    }

    @Override
    protected Integer GetId() {
        return this.generalID;
    }

    @Override
    protected void SetId(Integer id) {
        this.generalID = id;
    }
}
