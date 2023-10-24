package com.api.apiclientsdk;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

/**
 * 主类测试
 *
 */
class MainApplicationTests {

    public static void main(String[] args) throws IOException {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration("example.model");
        cu.addImport("java.util.List");

        ClassOrInterfaceDeclaration book = cu.addClass("Test");
        book.addField("String", "title");
        book.addField("Person", "author");
        book.addConstructor(Modifier.Keyword.PUBLIC)
                .addParameter("String", "title")
                .addParameter("Person", "author")
                .setBody(new BlockStmt()
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), "title"),
                                new NameExpr("title"),
                                AssignExpr.Operator.ASSIGN)))
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), "author"),
                                new NameExpr("author"),
                                AssignExpr.Operator.ASSIGN))));

        book.addMethod("getTitle", Modifier.Keyword.PUBLIC).setBody(
                new BlockStmt().addStatement(new ReturnStmt(new NameExpr("title"))));

        book.addMethod("getAuthor", Modifier.Keyword.PUBLIC).setBody(
                new BlockStmt().addStatement(new ReturnStmt(new NameExpr("author"))));

        File file = new File("D:\\api-platform-project\\api-client-sdk\\src\\main\\java\\com\\api\\apiclientsdk\\example\\Test.java");
        file.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream("D:/api-platform-project/api-client-sdk/src/main/java/com/api/apiclientsdk/example/Test.java", true);
        fileOutputStream.write(cu.toString().getBytes("UTF-8"));
        fileOutputStream.close();

    }


}
