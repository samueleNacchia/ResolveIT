DROP DATABASE IF EXISTS ResolveIt;
CREATE DATABASE ResolveIt;
USE ResolveIt;
SET GLOBAL max_connections = 500;

Create Table Cliente (
    ID_CL int primary key auto_increment,
    Nome varchar(30) not null,
    Cognome varchar(30) not null,
    Email varchar (50) not null,
    passwordCL varchar (255) not null,
    statoCl enum ('Attivo','Disattivato') not null default 'Attivo'
);

Create Table Gestore (
    Email varchar (50) primary key,
    passwordCL varchar (255) not null
);
Create Table Operatore (
    ID_OP int primary key auto_increment,
    Nome varchar(30) not null,
    Cognome varchar(30) not null,
    Email varchar (50) not null,
    passwordOP varchar (255) not null,
    statoOP enum ('Attivo','Disattivato') not null default 'Attivo'
);
Create Table Categoria (
    ID_C int primary key auto_increment,
    Nome varchar(30) not null,
    statoC enum ('Attiva','Disattivata') not null default 'Attiva'
);
Create Table Ticket (
    ID_T int primary key auto_increment,
    ID_CL int,
    ID_OP int default null,
    ID_C int,
    dataCreazione date not null,
    dataAnnullamento date default null,
    dataAssegnazione date default null,
    dataRisoluzione date default null,
    titolo varchar (100) not null,
    testo text not null,
    statoT enum ('Aperto','Annullato','Assegnato','Risolto') not null default 'Aperto',
    allegato mediumblob,
    foreign key (ID_CL) references Cliente(ID_CL) on delete cascade on update cascade,
    foreign key (ID_C) references Categoria(ID_C) on delete cascade on update cascade,
    foreign key (ID_OP) references Operatore(ID_OP) on delete cascade on update cascade
);

INSERT INTO Cliente (Nome, Cognome, Email, passwordCL, statoCl) VALUES
    ('Alessio', 'De Rosa', 'ale.derosa@email.com', 'password123', 'Attivo'),
    ('Vincenzo', 'Noviello', 'vin.noviello@email.com', 'password456', 'Attivo'),
    ('Claudia', 'Pirozzi', 'cla.pirozzi@email.com', 'sicura789', 'Attivo'),
    ('Mattia', 'Doronzo', 'mat.doronzo@email.com', 'vecchia_pass', 'Disattivato');

INSERT INTO Gestore (Email, passwordCL) VALUES
    ('admin@resolveit.com', 'admin_super_secret');

INSERT INTO Operatore (Nome, Cognome, Email, passwordOP) VALUES
    ('Enrico', 'Fattore', 'enr.fattore@resolveit.com', 'tech_pass1'),
    ('Carlo', 'Lodi', 'car.lodi@resolveit.com', 'tech_pass2');

INSERT INTO Categoria (Nome, statoC) VALUES
    ('Hardware', 'Attiva'),
    ('Software', 'Attiva'),
    ('Connettività', 'Attiva'),
    ('Amministrazione', 'Disattivata');


INSERT INTO Ticket (ID_CL, ID_OP, ID_C, dataCreazione, titolo, testo, statoT) VALUES
    (1, NULL, 2, '2023-10-25', 'Errore installazione Office', 'Non riesco ad installare il pacchetto.', 'Aperto'),
    (2, NULL, 1, '2023-10-26', 'Mouse rotto', 'Il tasto sinistro non funziona più.', 'Aperto');

INSERT INTO Ticket (ID_CL, ID_OP, ID_C, dataCreazione, dataAssegnazione, titolo, testo, statoT) VALUES
    (3, 1, 3, '2023-10-20', '2023-10-21', 'Internet lento', 'La connessione va a 2mbps.', 'Assegnato');

-- CASO 3: Ticket RISOLTO (Preso in carico e chiuso da Operatore 2)
INSERT INTO Ticket (ID_CL, ID_OP, ID_C, dataCreazione, dataAssegnazione, dataRisoluzione, titolo, testo, statoT) VALUES
    (1, 2, 2, '2023-09-01', '2023-09-02', '2023-09-03', 'Password dimenticata', 'Ho resettato la password.', 'Risolto');

-- CASO 4: Ticket ANNULLATO dal cliente (ID_OP NULL perché annullato prima della presa in carico)
INSERT INTO Ticket (ID_CL, ID_OP, ID_C, dataCreazione, dataAnnullamento, titolo, testo, statoT) VALUES
    (2, NULL, 1, '2023-10-10', '2023-10-11', 'Schermo nero', 'Nonostante il collegamento via cavo del monitor ottengo sempre schermata nera', 'Annullato');
