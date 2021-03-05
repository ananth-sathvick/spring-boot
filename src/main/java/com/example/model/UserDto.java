package com.example.model;

public class UserDto {
    
    private String email;
    private String password;
    private String fname;
    private String lname;
    private String roleName;

    public String getRoleName() {
        return this.roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }


    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFname() {
        return this.fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return this.lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }
    

    public User getUserFromDto(){
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFname(fname);
        user.setLname(lname);

        
        return user;
    }
    
}
