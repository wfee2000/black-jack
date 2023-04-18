CREATE TABLE PLAYER(
    user_name varchar(100) not null,
    user_pwd varchar(64) not null,
    constraint PK_playerKey PRIMARY KEY (user_name)
);

CREATE TABLE GAME(
    game_id INTEGER not null,
    game_name varchar(100),
    amount_player INTEGER not null,
    constraint PK_gameKey PRIMARY KEY (game_id)
);

CREATE TABLE GAMEPARTICIPATION(
    user_name varchar(100) not null,
    game_id INTEGER not null,
    user_points INTEGER,
    constraint PK_participationKey PRIMARY KEY (user_name, game_id),
    constraint FK_game FOREIGN KEY (game_id) REFERENCES GAME (game_id),
    constraint FK_user FOREIGN KEY (user_name) REFERENCES PLAYER (user_name)
);


CREATE TABLE LEADERBOARD(
    user_name varchar(100) not null,
    user_points INTEGER not null,
    constraint PK_leaderboardKey PRIMARY KEY (user_name, user_points),
    constraint FK_player FOREIGN KEY (user_name) REFERENCES PLAYER (user_name)
);