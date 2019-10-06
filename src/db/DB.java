package db;

import Model.BookIssueTM;
import Model.BookReturnTM;
import Model.BookTM;
import Model.MemberTM;

import java.util.ArrayList;

public class DB {
    public static ArrayList<MemberTM> members = new ArrayList<>();
    public static ArrayList<BookTM> books = new ArrayList<>();
    public static ArrayList<BookIssueTM> issued = new ArrayList<>();
    public static ArrayList<BookReturnTM> returned = new ArrayList<>();
}
