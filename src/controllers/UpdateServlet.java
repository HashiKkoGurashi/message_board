package controllers;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Message;
import models.validators.MessageValidator;
import utils.DBUtil;


@WebServlet("/update")
public class UpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public UpdateServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String _token = request.getParameter("_token");

        //トークン認証
        if (_token != null && _token.equals(request.getSession().getId())) {
            //トークン認証が通った場合のみ、データベース更新処理
            EntityManager em = DBUtil.createEntityManager();
            Message m = em.find(Message.class, (Integer)(request.getSession().getAttribute("message_id")));

            String title = request.getParameter("title");
            m.setTitle(title);

            String content = request.getParameter("content");
            m.setContent(content);

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            m.setUpdated_at(currentTime);

            //バリデーションを実行し、エラーがあったらeditページに戻す
            List<String> errors = MessageValidator.validate(m);
            if(errors.size()>0) {
                em.close();

                //エラーメッセージ ＆ 初期値を送信
                request.setAttribute("errors", errors);
                request.setAttribute("message", m);
                request.setAttribute("_token", request.getSession().getId());

                RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/messages/edit.jsp");
                rd.forward(request, response);

            } else {
                //バリデーションエラーがなければ、データベース更新
                em.getTransaction().begin();
                em.getTransaction().commit();
                request.getSession().setAttribute("flush", "更新が完了しました");
                em.close();

                //セッションスコープ上の不要になったデータを削除
                request.getSession().removeAttribute("message_id");

                //indexページへリダイレクト
                response.sendRedirect(request.getContextPath() + "/index");
            }
        }

    }

}
