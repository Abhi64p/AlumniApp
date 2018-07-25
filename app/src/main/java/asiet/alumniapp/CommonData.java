package asiet.alumniapp;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;

public class CommonData
{
    static final String SP = "asiet.alumniapp";
    static final String UserPoolId = "ap-south-1_0ku4twesK";
    static final String ClientId = "32omdhrbpnuj1va7vm26bhgeeo";
    static final String ClientSecret = "dj1mihh6b7ql369rfn6f0jfslb48jfe7culq3ttuo9lsn6tggd4";

    static final String CheckMailAddress = "https://squareskipper.000webhostapp.com/checkEmail.php";
    static final String CheckPhoneAddress = "https://squareskipper.000webhostapp.com/checkPhone.php";
    static final String InsertDetailsAddress = "https://squareskipper.000webhostapp.com/insertData.php";
    static final String VerifiedPhoneAddress = "https://squareskipper.000webhostapp.com/verifiedPhone.php";
    static final String CheckPasswordAddress = "https://squareskipper.000webhostapp.com/checkPassword.php";
    static final String IsPhoneVerified = "https://squareskipper.000webhostapp.com/isPhoneVerified.php";
    static final String IsEmailVerified = "https://squareskipper.000webhostapp.com/isEmailVerified.php";
    static final String VerifiedEmailAddress = "https://squareskipper.000webhostapp.com/verifiedEmail.php";

    static CognitoUser cognitoUser = null;
}
