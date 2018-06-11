package com.popularpenguin.runapp.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface RunDao {
    @Query("SELECT * FROM challenges")
    List<Challenge> getChallenges();

    @Query("SELECT * FROM challenges WHERE id = :id LIMIT 1")
    Challenge getChallengeById(long id);

    @Query("SELECT * FROM sessions")
    List<Session> getSessions();

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    Session getSessionById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertChallenges(List<Challenge> challengeList);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int insertChallenge(Challenge challenge);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertChallenges(Challenge[] challengeList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertSession(Session session);

    @Query("DELETE FROM sessions")
    int deleteSessions();
}
