package trabs.trab2.grupo1;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

import static trabs.trab2.grupo1.StreamUtils.forEachIf;

public class Student implements Comparable<Student> {
    private final String name;
    private final int number;
    private final String className;

    public Student(String fullDescription) {
        Scanner scanner = new Scanner(fullDescription);
        this.className = scanner.next();
        this.number = scanner.nextInt();
        this.name = scanner.nextLine().trim();
    }

    public String getName() {
        return this.name;
    }

    public int getNumber() {
        return this.number;
    }

    public String getClassName() {
        return this.className;
    }

    @Override
    public String toString() {
        return this.className + " " + this.number + " " + this.name;
    }

    @Override
    public int compareTo(Student o) {
        return this.number - o.number;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Student student)) return false;
        return this.className.equals(student.className) && this.number == student.number && this.name.equals(student.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.className, this.number, this.name);
    }

    public static int copyStudents(BufferedReader br, PrintWriter pw, String classId) throws IOException {
        return forEachIf(br,
            Student::new,
            (student) -> student.getClassName().equals(classId),
            (student -> pw.print(student.toString() + "\n"))
        );
    }

    public static int printStudents(String pathIn, String classId) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(pathIn))) {
            return copyStudents(br, new PrintWriter(System.out, true), classId);
        }
    }

    public static int copyStudents(String pathIn, String classId) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(pathIn));
             PrintWriter pw = new PrintWriter(new FileWriter(classId + ".txt"))) {
            return copyStudents(br, pw, classId);
        }
    }
}
